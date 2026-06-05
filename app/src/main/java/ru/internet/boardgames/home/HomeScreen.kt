package ru.internet.boardgames.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.internet.boardgames.R
import ru.internet.boardgames.ui.BoardGamesTheme

/**
 * Главный экран: сетка доступных настольных игр.
 *
 * Структура масштабируется: добавь игру в [gameItems] и передай
 * соответствующий callback в параметры функции.
 *
 * @param onNavigateToSpyGame  Навигация в фичу SpyGame.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSpyGame: () -> Unit,
    // onNavigateToSoundQuiz: () -> Unit,  ← будущая игра
    modifier: Modifier = Modifier
) {
    // Список игр — модель для сетки.
    // При добавлении Sound Quiz: добавить элемент и передать нужный callback.
    val games = listOf(
        GameEntry(
            emoji       = "🕵️",
            titleRes    = R.string.spy_game_title,
            descRes     = R.string.spy_game_description,
            onClick     = onNavigateToSpyGame
        )
        // GameEntry("🎵", R.string.sound_quiz_title, R.string.sound_quiz_description, onNavigateToSoundQuiz)
    )

    Scaffold(
        modifier = modifier,
        topBar   = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.app_name),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns            = GridCells.Adaptive(minSize = 160.dp),
            modifier           = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding     = PaddingValues(16.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                count = games.size,
                key   = { games[it].titleRes }
            ) { index ->
                GameCard(entry = games[index])
            }
        }
    }
}

// ─── Модель и компонент карточки ──────────────────────────────────────────────

private data class GameEntry(
    val emoji    : String,
    val titleRes : Int,
    val descRes  : Int,
    val onClick  : () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameCard(
    entry    : GameEntry,
    modifier : Modifier = Modifier
) {
    ElevatedCard(
        onClick   = entry.onClick,
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text     = entry.emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text       = stringResource(entry.titleRes),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = stringResource(entry.descRes),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BoardGamesTheme {
        HomeScreen(onNavigateToSpyGame = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenMultipleGamesPreview() {
    BoardGamesTheme {
        // Демонстрирует как выглядит сетка с двумя играми
        HomeScreen(onNavigateToSpyGame = {})
    }
}
