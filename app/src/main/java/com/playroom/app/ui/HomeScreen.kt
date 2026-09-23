package com.playroom.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playroom.app.model.Game
import com.playroom.app.ui.theme.DeepBackground
import com.playroom.app.ui.theme.NeonGreen
import com.playroom.app.ui.theme.NeonPurple
import com.playroom.app.ui.theme.PlayRoomTheme

/**
 * Phase 4: stateless screen. All state comes from [LobbyViewModel] and all
 * events go back to it, so the UI is easy to preview and test.
 */
@Composable
fun HomeScreen(
    onJoinGame: (Game) -> Unit = {},
    viewModel: LobbyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ----- Header -----
        Text(
            text = "PLAYROOM",
            color = NeonGreen,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Real-Time Gaming Lobby",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ----- Search (Phase 3 + 4: live filtering via ViewModel) -----
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text("Search games...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = { Text(text = "🔍", fontSize = 18.sp) },
            trailingIcon = {
                if (state.isSearchActive) {
                    Text(
                        text = "✕",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.clearSearch() }
                            .padding(8.dp)
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ----- "Live Games" section title -----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = NeonGreen, shape = RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isSearchActive) "Results" else "🔥 Live Games",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ----- Game cards -----
        if (state.games.isEmpty()) {
            Text(
                text = "No games match \"${state.searchQuery.trim()}\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.games.forEach { game ->
                GameCard(game = game, onJoin = onJoinGame)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GameCard(game: Game, onJoin: (Game) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${game.emoji} ${game.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                CategoryChip(category = game.category)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${game.maxPlayers} players max",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onJoin(game) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = DeepBackground
                )
            ) {
                Text(text = "JOIN ROOM", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = NeonPurple.copy(alpha = 0.18f)
    ) {
        Text(
            text = category,
            color = NeonPurple,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B14)
@Composable
private fun HomeScreenPreview() {
    PlayRoomTheme {
        HomeScreen(viewModel = LobbyViewModel())
    }
}
