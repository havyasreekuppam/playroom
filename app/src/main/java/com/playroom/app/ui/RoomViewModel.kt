package com.playroom.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playroom.app.data.ConnectionState
import com.playroom.app.data.RoomState
import com.playroom.app.data.SocketRoomRepository
import com.playroom.app.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Everything the RoomScreen needs. [game] is the game whose JOIN ROOM was
 * pressed; [room] only becomes "joined" once the server confirms.
 */
data class RoomUiState(
    val game: Game? = null,
    val room: RoomState = RoomState(),
    val connection: ConnectionState = ConnectionState.Connecting,
    val isJoining: Boolean = false
)

/** Phase 7: owns the socket repository for a room session. */
class RoomViewModel(
    private val repository: SocketRoomRepository = SocketRoomRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    init {
        // Mirror repository state into UI state.
        viewModelScope.launch {
            repository.roomState.collect { room ->
                _state.update { it.copy(room = room, isJoining = false) }
            }
        }
        viewModelScope.launch {
            repository.connectionState.collect { conn ->
                _state.update { it.copy(connection = conn) }
            }
        }
    }

    /** Called by HomeScreen right before navigating to the room. */
    fun selectGame(game: Game) {
        _state.update { it.copy(game = game) }
    }

    /** Phase 7: JOIN ROOM -> emit join_room. Server confirms via joined_room. */
    fun joinRoom(playerName: String) {
        val game = _state.value.game ?: return
        if (_state.value.room.isJoined || _state.value.isJoining) return
        _state.update { it.copy(isJoining = true) }
        repository.joinRoom(gameId = game.id, playerName = playerName)
    }
}
