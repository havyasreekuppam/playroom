package com.playroom.app.ui

import androidx.lifecycle.ViewModel
import com.playroom.app.model.Game
import com.playroom.app.model.GameCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * All state the lobby screen needs. One immutable object -> easy to render
 * and easy to reason about.
 */
data class LobbyUiState(
    val searchQuery: String = "",
    val games: List<Game> = GameCatalog.games
) {
    val isSearchActive: Boolean get() = searchQuery.isNotBlank()
}

/**
 * Phase 4: MVVM. The screen observes [state] and calls methods on this
 * ViewModel; it never owns state itself.
 */
class LobbyViewModel : ViewModel() {

    private val _state = MutableStateFlow(LobbyUiState())
    val state: StateFlow<LobbyUiState> = _state.asStateFlow()

    /** Called on every keystroke in the search field. Filters in memory. */
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterGames(query)
    }

    /** Clears the search field and shows the full catalog again. */
    fun clearSearch() {
        onSearchQueryChange("")
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
