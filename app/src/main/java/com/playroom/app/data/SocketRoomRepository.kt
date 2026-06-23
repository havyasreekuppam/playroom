package com.playroom.app.data

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Phase 6: single Socket.IO connection for the whole app, wrapped in
 * Kotlin Flows so ViewModels never touch socket internals.
 *
 * Server events consumed here: joined_room, room_update, receive_message,
 * left_room, room_error, connect/disconnect.
 *
 * The default URL is the Android emulator alias for the host machine
 * (10.0.2.2); a real device should use the computer's LAN IP.
 */
class SocketRoomRepository(serverUrl: String = DEFAULT_SERVER_URL) {

    companion object {
        /** 10.0.2.2 = host loopback from the Android emulator. */
        const val DEFAULT_SERVER_URL = "http://10.0.2.2:3000"
        private const val TAG = "SocketRoomRepo"
    }

    private val socket: Socket = IO.socket(serverUrl)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _roomState = MutableStateFlow(RoomState())
    val roomState: StateFlow<RoomState> = _roomState.asStateFlow()

    private val _messages = MutableSharedFlow<ChatMessage>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<ChatMessage> = _messages.asSharedFlow()

    /** One-shot errors surfaced to the UI as a snackbar/dialog. */
    private val _errors = MutableSharedFlow<String>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    init {
        registerListeners()
        socket.connect()
    }

    private fun registerListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            _connectionState.value = ConnectionState.Connected
        }.on(Socket.EVENT_DISCONNECT) { args ->
            _connectionState.value = ConnectionState.Disconnected
            // Server is gone or network dropped: leave the room locally too.
            _roomState.value = RoomState()
            Log.w(TAG, "disconnected: ${args.firstOrNull()}")
        }.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val reason = args.firstOrNull()?.toString() ?: "unknown error"
            _connectionState.value = ConnectionState.Failed(reason)
            Log.e(TAG, "connect error: $reason")
        }
            .on("joined_room") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                _roomState.value = json.toRoomState(yourName = json.optString("yourName"))
            }
            .on("room_update") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val updated = json.toRoomState()
                // Keep yourName from joined_room; room_update doesn't carry it.
                _roomState.value = updated.copy(yourName = _roomState.value.yourName)
            }
            .on("left_room") {
                _roomState.value = RoomState()
            }
            .on("receive_message") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val sender = json.optString("sender")
                val message = ChatMessage(
                    sender = sender,
                    text = json.optString("text"),
                    sentAt = json.optLong("sentAt"),
                    isMine = sender == _roomState.value.yourName
                )
                _messages.tryEmit(message)
            }
            .on("room_error") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                _errors.tryEmit(json.optString("message", "Something went wrong"))
            }
    }

    // ----- Actions called by ViewModels -----

    fun joinRoom(gameId: String, playerName: String) {
        val payload = JSONObject().put("gameId", gameId).put("playerName", playerName)
        socket.emit("join_room", payload)
    }

    fun leaveRoom() {
        socket.emit("leave_room")
    }

    fun sendMessage(text: String) {
        socket.emit("send_message", JSONObject().put("text", text))
    }

    /** Called when the Activity is destroyed for good. */
    fun disconnect() {
        socket.disconnect()
    }

    // ----- JSON parsing helpers -----

    private fun JSONObject.toRoomState(yourName: String? = null): RoomState {
        val playersJson = optJSONArray("players") ?: JSONArray()
        val players = buildList {
            for (i in 0 until playersJson.length()) {
                val p = playersJson.getJSONObject(i)
                add(Player(id = p.optString("id"), name = p.optString("name")))
            }
        }
        return RoomState(
            roomId = optString("roomId"),
            gameId = optString("gameId"),
            maxPlayers = optInt("maxPlayers"),
            players = players,
            yourName = yourName ?: _roomState.value.yourName
        )
    }
}
