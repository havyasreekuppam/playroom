package com.playroom.app.data

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** Live socket connection status shown in the UI. */
sealed interface ConnectionState {
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
    data object Disconnected : ConnectionState
    data class Failed(val reason: String) : ConnectionState
}

/** One player inside a room, as reported by the server. */
data class Player(val id: String, val name: String)

/** Full state of the room the local player is in. */
data class RoomState(
    val roomId: String = "",
    val gameId: String = "",
    val maxPlayers: Int = 0,
    val players: List<Player> = emptyList(),
    val yourName: String = ""
) {
    val isJoined: Boolean get() = roomId.isNotEmpty()
}

/** One chat message in the room. */
data class ChatMessage(
    val sender: String,
    val text: String,
    val sentAt: Long,
    val isMine: Boolean
)
