package com.playroom.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playroom.app.data.SocketRoomRepository
import com.playroom.app.model.Game
import com.playroom.app.model.GameCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * All state the lobby screen needs. One immutable object -> easy to render
 * and easy to reason about.
 */
data class LobbyUiState(
    val searchQuery: String = "",
    val games: List<Game> = GameCatalog.games,
    /** gameId -> live player count from the server (rooms_snapshot). */
    val playerCounts: Map<String, Int> = emptyMap()
) {
    val isSearchActive: Boolean get() = searchQuery.isNotBlank()
}

/**
 * Phase 4: MVVM. The screen observes [state] and calls methods on this
 * ViewModel; it never owns state itself.
 */
class LobbyViewModel(
    private val repository: SocketRoomRepository = SocketRoomRepository.shared
) : ViewModel() {

    private val _state = MutableStateFlow(LobbyUiState())
    val state: StateFlow<LobbyUiState> = _state.asStateFlow()

    init {
        // Live lobby: the server pushes a rooms_snapshot on every join/leave.
        viewModelScope.launch {
            repository.lobbyRooms.collect { counts ->
                _state.update { it.copy(playerCounts = counts) }
            }
        }
    }

    /** Called on every keystroke in the search field. Filters in memory. */
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterGames(query)
    }

    /** Clears the search field and shows the full catalog again. */
    fun clearSearch() {
        onSearchQueryChange("")
    }

    override fun onCleared() {
        super.onCleared()
        // Safety net for sessions that never opened a room screen:
        // make sure the shared socket cannot outlive the UI.
        repository.disconnect()
    }

    private fun filterGames(query: String) {
        val trimmed = query.trim()
        _state.update { current ->
            current.copy(
                games = if (trimmed.isEmpty()) {
                    GameCatalog.games
                } else {
                    GameCatalog.games.filter { game ->
                        game.name.contains(trimmed, ignoreCase = true) ||
                            game.category.contains(trimmed, ignoreCase = true)
                    }
                }
            )
        }
    }
}
