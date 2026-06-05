package ru.internet.boardgames.spygame.presentation.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.spygame.R
import ru.internet.boardgames.spygame.domain.usecase.SaveSettingsUseCase
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState             = uiState,
        onBack              = onBack,
        onPlayerCountChange = viewModel::setPlayerCount,
        onLanguageChange    = viewModel::setLanguage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onPlayerCountChange: (Int) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        AnimatedVisibility(
            visible = uiState.isLoading,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSection(title = stringResource(R.string.players_section_title)) {
                    PlayerCountControl(
                        count       = uiState.playerCount,
                        min         = SaveSettingsUseCase.MIN_PLAYERS,
                        max         = SaveSettingsUseCase.MAX_PLAYERS,
                        onIncrement = { onPlayerCountChange(uiState.playerCount + 1) },
                        onDecrement = { onPlayerCountChange(uiState.playerCount - 1) }
                    )
                }
                SettingsSection(title = stringResource(R.string.language_section_title)) {
                    LanguageSelector(
                        selected = uiState.language,
                        onSelect = onLanguageChange
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.labelLarge,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) { content() }
        }
    }
}

@Composable
private fun PlayerCountControl(
    count: Int,
    min: Int,
    max: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier            = modifier,
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        FilledTonalIconButton(
            onClick  = onDecrement,
            enabled  = count > min,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.decrease_players_desc)
            )
        }

        AnimatedContent(
            targetState  = count,
            transitionSpec = {
                if (targetState > initialState)
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                else
                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
            },
            label = "PlayerCountAnim"
        ) { targetCount ->
            Text(
                text       = targetCount.toString(),
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        FilledTonalIconButton(
            onClick  = onIncrement,
            enabled  = count < max,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.increase_players_desc)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        AppLanguage.SYSTEM  to stringResource(R.string.language_system_label),
        AppLanguage.RUSSIAN to stringResource(R.string.language_russian_label),
        AppLanguage.ENGLISH to stringResource(R.string.language_english_label)
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (language, label) ->
            SegmentedButton(
                shape    = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                selected = selected == language,
                onClick  = { onSelect(language) },
                label    = { Text(text = label, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    SpyGameTheme {
        SettingsContent(
            uiState             = SettingsUiState(playerCount = 6, language = AppLanguage.SYSTEM, isLoading = false),
            onBack              = {},
            onPlayerCountChange = {},
            onLanguageChange    = {}
        )
    }
}
