package ru.internet.boardgames.soundquiz.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.internet.boardgames.soundquiz.domain.model.GameSettings
import ru.internet.boardgames.soundquiz.domain.usecase.GetCategoriesUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.GetSoundQuizSettingsUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.SaveSoundQuizSettingsUseCase
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSoundQuizSettingsUseCase,
    private val saveSettings: SaveSoundQuizSettingsUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _pendingSettings = MutableStateFlow<GameSettings?>(null)

    init {
        // Язык фиксирован из локали — используем combine вместо flatMapLatest
        val lang = resolveLanguageCode()
        viewModelScope.launch {
            combine(
                getSettings(),
                getCategories(lang)
            ) { settings, categories -> settings to categories }
                .collect { (settings, categories) ->
                    _uiState.update { it.copy(
                        settings   = settings,
                        categories = categories,
                        isLoading  = false
                    )}
                }
        }

        // Дебаунс 300мс: запись в DataStore после паузы в изменениях
        viewModelScope.launch {
            _pendingSettings
                .filterNotNull()
                .debounce(300L)
                .collect { settings -> saveSettings(settings) }
        }
    }

    fun onNumberOfCategoriesChanged(n: Int) {
        updateSettings { settings ->
            val trimmedPinned = settings.pinnedCategoryIds.take(n).toSet()
            settings.copy(numberOfCategories = n, pinnedCategoryIds = trimmedPinned)
        }
    }

    fun onWordsPerCategoryChanged(m: Int) {
        updateSettings { it.copy(wordsPerCategory = m) }
    }

    fun onTimerSecondsChanged(seconds: Int) {
        updateSettings { it.copy(timerSeconds = seconds) }
    }

    fun onCategoryToggle(categoryId: Long) {
        updateSettings { settings ->
            val pinned = settings.pinnedCategoryIds.toMutableSet()
            if (categoryId in pinned) {
                pinned.remove(categoryId)
            } else if (pinned.size < settings.numberOfCategories) {
                pinned.add(categoryId)
            }
            settings.copy(pinnedCategoryIds = pinned)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(categorySearchQuery = query) }
    }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val updated = transform(_uiState.value.settings)
        _uiState.update { it.copy(settings = updated) }
        _pendingSettings.value = updated
    }

    private fun resolveLanguageCode(): String =
        if (Locale.getDefault().language == "ru") "ru" else "en"
}
