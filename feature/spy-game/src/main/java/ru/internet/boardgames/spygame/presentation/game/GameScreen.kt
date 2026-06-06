package ru.internet.boardgames.spygame.presentation.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.spygame.R
import ru.internet.boardgames.spygame.presentation.game.components.CardStack
import ru.internet.boardgames.spygame.presentation.game.components.RefreshButton
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onOpenSettings: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GameContent(
        uiState        = uiState,
        onOpenSettings = onOpenSettings,
        onCardTap      = viewModel::onCardTap,
        onCardSwiped   = viewModel::onCardSwiped,
        onRefresh      = viewModel::refreshGame
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameContent(
    uiState: GameUiState,
    onOpenSettings: () -> Unit,
    onCardTap: (Int) -> Unit,
    onCardSwiped: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.app_name),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Настройки — обычный IconButton (менее акцентированный)
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector        = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings_action_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        AnimatedContent(
            targetState    = ContentState.from(uiState),
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            label          = "GameContentState",
            contentKey     = { it.name }
        ) { contentState ->
            when (contentState) {
                ContentState.LOADING       -> LoadingContent()
                ContentState.ERROR         -> ErrorContent(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = onRefresh
                )
                ContentState.GAME_COMPLETE -> GameCompleteContent(
                    onNewGame = onRefresh,
                    isLoading = uiState.isLoading
                )
                ContentState.PLAYING       -> {
                    val session = uiState.session ?: return@AnimatedContent
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CardStack(
                            session            = session,
                            cardStates         = uiState.cardStates,
                            topStackedPosition = uiState.topStackedPosition,
                            timerProgress      = uiState.timerProgress,
                            onCardTap          = onCardTap,
                            onCardSwiped       = onCardSwiped,
                            modifier           = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        )
                        RefreshButton(
                            onClick   = onRefresh,
                            isLoading = uiState.isLoading,
                            modifier  = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

private enum class ContentState {
    LOADING, ERROR, PLAYING, GAME_COMPLETE;
    companion object {
        fun from(s: GameUiState) = when {
            s.isLoading       -> LOADING
            s.error != null   -> ERROR
            s.isGameComplete  -> GAME_COMPLETE
            s.session != null -> PLAYING
            else              -> LOADING
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.loading_game),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column(    modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.SentimentDissatisfied, null,
                Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun GameCompleteContent(
    onNewGame: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.game_complete_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.game_complete_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            RefreshButton(onClick = onNewGame, isLoading = isLoading)
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "С FilledTonalIconButton счётчика")
@Composable
private fun WithCounterPreview() {
    SpyGameTheme {
        GameContent(GameUiState(isLoading = true), {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Без кнопки счётчика")
@Composable
private fun WithoutCounterPreview() {
    SpyGameTheme {
        GameContent(GameUiState(isLoading = true), {}, {}, {}, {})
    }
}
