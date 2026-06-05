package ru.internet.boardgames.spygame.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.internet.boardgames.spygame.domain.usecase.GetSettingsUseCase
import ru.internet.boardgames.spygame.domain.usecase.SaveSettingsUseCase
import javax.inject.Inject

/**
 * ViewModel экрана настроек.
 *
 * Подписывается на [GetSettingsUseCase] и реактивно обновляет [uiState].
 * Запись в DataStore — через [SaveSettingsUseCase] — происходит немедленно
 * при каждом изменении: отдельная кнопка «Сохранить» не нужна.
 *
 * Почему [MutableStateFlow] вместо [stateIn]:
 * - Нам нужен начальный [SettingsUiState] с [isLoading] = true до первого эмита.
 * - [stateIn] с [SharingStarted.WhileSubscribed] может давать нежелательные
 *   реэмиты при пересоздании подписчика. [MutableStateFlow] + [update] проще.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    // ─────────────────────────────────────────
    // Наблюдение за настройками
    // ─────────────────────────────────────────

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        playerCount = settings.playerCount,
                        language = AppLanguage.fromCode(settings.language),
                        isLoading = false
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // Действия пользователя
    // ─────────────────────────────────────────

    /**
     * Изменяет число игроков.
     * [SaveSettingsUseCase] зажимает значение в [2..10] — здесь дополнительная
     * валидация не нужна, но UI всё равно блокирует кнопки на граничных значениях.
     */
    fun setPlayerCount(count: Int) {
        viewModelScope.launch {
            saveSettingsUseCase.savePlayerCount(count)
        }
    }

    /** Изменяет язык. Изменение сразу отражается в GameViewModel через Flow. */
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            saveSettingsUseCase.saveLanguage(language.code)
        }
    }
}
