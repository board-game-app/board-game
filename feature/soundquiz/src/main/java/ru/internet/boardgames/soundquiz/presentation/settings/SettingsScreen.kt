package ru.internet.boardgames.soundquiz.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.soundquiz.R
import ru.internet.boardgames.soundquiz.domain.model.Category
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sound_quiz_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sound_quiz_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            // Column + verticalScroll вместо LazyColumn:
            // избегаем лишнего lazy-измерения при каждом изменении слайдера.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // ── Параметры игры ───────────────────────────────────────────
                SectionTitle(stringResource(R.string.sound_quiz_settings_game_params))

                NumberPickerItem(
                    label         = stringResource(R.string.sound_quiz_settings_num_stacks),
                    value         = uiState.settings.numberOfCategories,
                    range         = 1..5,
                    onValueChange = viewModel::onNumberOfCategoriesChanged,
                    modifier      = Modifier.padding(horizontal = 16.dp)
                )
                NumberPickerItem(
                    label         = stringResource(R.string.sound_quiz_settings_words_per_stack),
                    value         = uiState.settings.wordsPerCategory,
                    range         = 1..10,
                    onValueChange = viewModel::onWordsPerCategoryChanged,
                    modifier      = Modifier.padding(horizontal = 16.dp)
                )
                TimerSliderItem(
                    label                  = stringResource(R.string.sound_quiz_settings_timer),
                    timerSecondsFromState  = uiState.settings.timerSeconds,
                    onTimerSecondsChanged  = viewModel::onTimerSecondsChanged,
                    modifier               = Modifier.padding(horizontal = 16.dp)
                )

                // ── Категории ────────────────────────────────────────────────
                SectionTitle(stringResource(R.string.sound_quiz_settings_categories))

                val selected = uiState.settings.pinnedCategoryIds.size
                val max      = uiState.settings.numberOfCategories
                Text(
                    text = stringResource(R.string.sound_quiz_settings_categories_selected, selected, max),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = uiState.categorySearchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder   = { Text(stringResource(R.string.sound_quiz_settings_search_hint)) },
                    leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine    = true,
                    shape         = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(8.dp))

                if (uiState.filteredCategories.isEmpty()) {
                    Text(
                        text     = stringResource(R.string.sound_quiz_settings_no_categories),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    // Не-ленивый рендер: ~20 категорий рендерятся мгновенно,
                    // зато нет overhead LazyColumn при изменении слайдера
                    uiState.filteredCategories.forEach { category ->
                        val isChecked = category.id in uiState.settings.pinnedCategoryIds
                        CategoryCheckboxItem(
                            category  = category,
                            isChecked = isChecked,
                            isEnabled = isChecked || !uiState.isPinnedLimitReached,
                            onToggle  = { viewModel.onCategoryToggle(category.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Column {
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text     = title,
            style    = MaterialTheme.typography.titleMedium,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun NumberPickerItem(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier           = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment  = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first
        ) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.sound_quiz_settings_decrease))
        }
        Text(
            text      = value.toString(),
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier  = Modifier.width(40.dp)
        )
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.sound_quiz_settings_increase))
        }
    }
}

/**
 * Ползунок таймера с локальным состоянием.
 * Локальный [localValue] обновляется немедленно при перетаскивании — без вызова ViewModel.
 * [onTimerSecondsChanged] вызывается ТОЛЬКО по окончании жеста (onValueChangeFinished),
 * что устраняет лаг скролла от 50+ обновлений состояния в секунду.
 */
@Composable
private fun TimerSliderItem(
    label: String,
    timerSecondsFromState: Int,
    onTimerSecondsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Локальное состояние следит за sliderValue независимо от ViewModel
    var localValue by remember(timerSecondsFromState) { mutableStateOf(timerSecondsFromState.toFloat()) }
    val displaySeconds = localValue.roundToInt().coerceIn(5, 300)

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier               = Modifier.fillMaxWidth(),
            horizontalArrangement  = Arrangement.SpaceBetween,
            verticalAlignment      = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text  = stringResource(R.string.sound_quiz_settings_timer_value, displaySeconds),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value               = localValue,
            onValueChange       = { localValue = it }, // только локально — без ViewModel
            onValueChangeFinished = {
                val snapped = (localValue / 5f).roundToInt() * 5
                val clamped = snapped.coerceIn(5, 300)
                localValue = clamped.toFloat()
                onTimerSecondsChanged(clamped)
            },
            valueRange          = 5f..300f,
            steps               = 58,
            modifier            = Modifier.fillMaxWidth()
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.sound_quiz_settings_timer_min), style = MaterialTheme.typography.labelSmall)
            Text(stringResource(R.string.sound_quiz_settings_timer_max), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CategoryCheckboxItem(
    category: Category,
    isChecked: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { onToggle() }, enabled = isEnabled)
        Box(Modifier.size(12.dp).background(Color(category.colorArgb), CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text  = pluralStringResource(R.plurals.sound_quiz_word_count, category.wordCount, category.wordCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (category.isBuiltIn) {
            Icon(
                Icons.Default.Lock,
                contentDescription = stringResource(R.string.sound_quiz_built_in),
                modifier           = Modifier.size(16.dp),
                tint               = MaterialTheme.colorScheme.outline
            )
        }
    }
}
