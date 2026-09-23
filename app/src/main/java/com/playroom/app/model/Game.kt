package com.playroom.app.model

/**
 * A game that can be joined from the lobby.
 *
 * @param id         stable id used by the backend to identify the room (e.g. "ludo")
 * @param name       display name shown on the card
 * @param category   small chip label, e.g. "Casual"
 * @param emoji      simple icon so we don't need image assets
 * @param maxPlayers server enforces the same limit
 */
data class Game(
    val id: String,
    val name: String,
    val category: String,
    val emoji: String,
    val maxPlayers: Int
)

/**
 * The static game catalog. In a bigger app this would come from an API;
 * for PlayRoom the lobby list is fixed and the backend owns live player counts.
 */
object GameCatalog {
    val games = listOf(
        Game(id = "ludo", name = "Ludo", category = "Casual", emoji = "🎲", maxPlayers = 8),
        Game(id = "chess", name = "Chess", category = "Strategy", emoji = "♟️", maxPlayers = 4),
        Game(id = "quiz-battle", name = "Quiz Battle", category = "Quiz", emoji = "🧠", maxPlayers = 10)
    )
}
