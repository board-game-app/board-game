package ru.internet.boardgames.soundquiz.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.soundquiz.R
import ru.internet.boardgames.soundquiz.domain.model.CardStack
import ru.internet.boardgames.soundquiz.domain.model.GameSession
import ru.internet.boardgames.soundquiz.presentation.game.components.ActiveCardView
import ru.internet.boardgames.soundquiz.presentation.game.components.CategoryStackCard
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sound_quiz_game_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings,
                            contentDescription = stringResource(R.string.sound_quiz_settings_action))
                    }
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Edit,
                            contentDescription = stringResource(R.string.sound_quiz_categories_action))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    ErrorContent(
                        message  = uiState.error ?: stringResource(R.string.sound_quiz_unknown_error),
                        onRetry  = viewModel::onNewGame,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                uiState.isGameOver && uiState.session != null -> {
                    GameOverContent(
                        totalExplained = uiState.session!!.totalExplained,
                        onNewGame      = viewModel::onNewGame,
                        onSettings     = onNavigateToSettings,
                        modifier       = Modifier.fillMaxSize()
                    )
                }
                uiState.session != null -> {
                    GameContent(
                        session          = uiState.session!!,
                        uiState          = uiState,
                        onStackTap       = viewModel::onStackTap,
                        onCardTap        = viewModel::onCardTap,
                        onWordExplained  = viewModel::onWordExplained,
                        onFirstWordPress = viewModel::onFirstWordPress
                    )
                    if (uiState.passPhoneMessage) {
                        PassPhoneOverlay(
                            onReady  = viewModel::onReadyForNextPlayer,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GameContent(
    session: GameSession,
    uiState: GameUiState,
    onStackTap: (Int) -> Unit,
    onCardTap: () -> Unit,
    onWordExplained: () -> Unit,
    onFirstWordPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCard = session.activeCard

    if (activeCard == null) {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text     = stringResource(R.string.sound_quiz_select_stack),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            StacksGrid(
                stacks     = session.stacks,
                isEnabled  = true,
                onStackTap = onStackTap,
                modifier   = Modifier.weight(1f).fillMaxWidth()
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            ActiveCardView(
                activeCard       = activeCard,
                categoryName     = session.stacks[activeCard.stackIndex].category.name,
                cardColor        = session.stacks[activeCard.stackIndex].colorArgb,
                timerProgress    = uiState.timerProgress,
                timerRemainingMs = uiState.timerRemainingMs,
                onCardTap        = onCardTap,
                onWordExplained  = onWordExplained,
                onFirstWordPress = onFirstWordPress,
                modifier         = Modifier.weight(1f).fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            StacksGrid(
                stacks     = session.stacks,
                isEnabled  = false,
                onStackTap = {},
                modifier   = Modifier.fillMaxWidth().alpha(0.4f).padding(bottom = 8.dp),
                maxHeight  = 120.dp
            )
        }
    }
}

@Composable
private fun StacksGrid(
    stacks: List<CardStack>,
    isEnabled: Boolean,
    onStackTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    LazyVerticalGrid(
        columns               = GridCells.Adaptive(minSize = 120.dp),
        modifier              = if (maxHeight != androidx.compose.ui.unit.Dp.Unspecified)
            modifier.height(maxHeight) else modifier,
        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        userScrollEnabled     = false
    ) {
        items(stacks.size) { index ->
            CategoryStackCard(stack = stacks[index], isEnabled = isEnabled,
                onClick = { onStackTap(index) })
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overlay «Передайте телефон»
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Полноэкранный overlay.
 * Исправления:
 * 1. pointerInput(detectTapGestures) — поглощает ВСЕ тапы, предотвращая
 *    прохождение касаний к карточке на заднем плане и случайный запуск таймера.
 * 2. Надпись по центру, кнопка внизу — оба элемента хорошо видны.
 */
@Composable
private fun PassPhoneOverlay(onReady: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.88f))
            // Поглощаем все тапы — карточка позади не реагирует
            .pointerInput(Unit) { detectTapGestures { /* consume */ } }
    ) {
        // Надпись — по центру экрана
        Text(
            text      = stringResource(R.string.sound_quiz_pass_phone),
            style     = MaterialTheme.typography.headlineMedium,
            color     = Color.White,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        )
        // Кнопка — внизу, занимает всю ширину
        Button(
            onClick  = onReady,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp)
        ) {
            Text(
                text  = stringResource(R.string.sound_quiz_ready),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Экран результатов
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GameOverContent(
    totalExplained: Int,
    onNewGame: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier               = modifier.padding(32.dp),
        horizontalAlignment    = Alignment.CenterHorizontally,
        verticalArrangement    = Arrangement.Center
    ) {
        Text(stringResource(R.string.sound_quiz_game_over_emoji),
            style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.sound_quiz_game_over),
            style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text  = stringResource(R.string.sound_quiz_words_explained, totalExplained),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.sound_quiz_new_game))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.sound_quiz_change_settings))
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.sound_quiz_error, message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.sound_quiz_retry)) }
    }
}
