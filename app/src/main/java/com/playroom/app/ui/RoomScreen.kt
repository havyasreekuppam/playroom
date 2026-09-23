package com.playroom.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playroom.app.data.ConnectionState
import com.playroom.app.ui.theme.DangerRed
import com.playroom.app.ui.theme.DeepBackground
import com.playroom.app.ui.theme.NeonGreen
import com.playroom.app.ui.theme.NeonPurple

/**
 * Phase 7: room screen with name entry, JOIN ROOM and the player list.
 */
@Composable
fun RoomScreen(viewModel: RoomViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        val game = state.game
        if (game == null) {
            Text("No game selected")
            return@Column
        }

        // ----- Header: game name + connection dot -----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${game.emoji} ${game.name}",
                style = MaterialTheme.typography.headlineMedium,
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            ConnectionDot(connection = state.connection)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (val c = state.connection) {
                is ConnectionState.Connected -> "Connected"
                is ConnectionState.Connecting -> "Connecting..."
                is ConnectionState.Disconnected -> "Disconnected"
                is ConnectionState.Failed -> "Connection failed: ${c.reason}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!state.room.isJoined) {
            JoinSection(
                gameName = game.name,
                isJoining = state.isJoining,
                connection = state.connection,
                onJoin = viewModel::joinRoom
            )
        } else {
            PlayersCard(state = state)
        }
    }
}

@Composable
private fun JoinSection(
    gameName: String,
    isJoining: Boolean,
    connection: ConnectionState,
    onJoin: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Join $gameName",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a display name for the room.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Display name") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onJoin(name) },
                enabled = !isJoining && connection is ConnectionState.Connected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DeepBackground
                )
            ) {
                Text(
                    text = if (isJoining) "JOINING..." else "JOIN ROOM",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun PlayersCard(state: RoomUiState) {
    val room = state.room
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Players",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NeonGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${room.players.size} / ${room.maxPlayers}",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            room.players.forEach { player ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (player.name == room.yourName) NeonGreen else NeonPurple,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (player.name == room.yourName) "${player.name} (you)" else player.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionDot(connection: ConnectionState) {
    val color = when (connection) {
        is ConnectionState.Connected -> NeonGreen
        is ConnectionState.Connecting -> NeonPurple
        is ConnectionState.Disconnected -> androidx.compose.ui.graphics.Color.Gray
        is ConnectionState.Failed -> DangerRed
    }
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color = color, shape = CircleShape)
    )
}
