package com.playroom.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.playroom.app.ui.HomeScreen
import com.playroom.app.ui.RoomScreen
import com.playroom.app.ui.RoomViewModel
import com.playroom.app.ui.theme.PlayRoomTheme

/**
 * Tiny manual navigation: two screens, no navigation library.
 * lobby -> (JOIN ROOM) -> room -> (back) -> lobby
 */
class MainActivity : ComponentActivity() {

    private val roomViewModel: RoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayRoomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

    @Composable
    private fun AppContent() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Lobby) }

        when (val screen = currentScreen) {
            is Screen.Lobby -> HomeScreen(
                onJoinGame = { game ->
                    roomViewModel.selectGame(game)
                    currentScreen = Screen.Room
                }
            )

            is Screen.Room -> RoomScreen(
                viewModel = roomViewModel,
                onLeave = { currentScreen = Screen.Lobby }
            )
        }
    }
}

private sealed interface Screen {
    data object Lobby : Screen
    data object Room : Screen
}
