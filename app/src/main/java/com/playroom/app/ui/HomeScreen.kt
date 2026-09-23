package com.playroom.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playroom.app.ui.theme.DeepBackground
import com.playroom.app.ui.theme.NeonGreen
import com.playroom.app.ui.theme.NeonPurple
import com.playroom.app.ui.theme.PlayRoomTheme

/**
 * Phase 1: static home screen.
 * Games are hardcoded for now - Phase 2 moves them into a Game data model.
 * The JOIN ROOM button does nothing yet - it becomes real in Phase 7.
 */
@Composable
fun HomeScreen() {
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
                text = "🔥 Live Games",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ----- Game cards (static in Phase 1) -----
        GameCard(name = "Ludo", category = "Casual", players = "4 / 8 players")
        Spacer(modifier = Modifier.height(16.dp))
        GameCard(name = "Chess", category = "Strategy", players = "2 / 4 players")
        Spacer(modifier = Modifier.height(16.dp))
        GameCard(name = "Quiz Battle", category = "Quiz", players = "6 / 10 players")
    }
}

@Composable
private fun GameCard(name: String, category: String, players: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                CategoryChip(category = category)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = players,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Phase 7: emit "join_room" through the Socket.IO repository
                },
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
        HomeScreen()
    }
}
