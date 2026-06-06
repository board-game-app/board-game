package ru.internet.boardgames.spygame.presentation.settings


// ─────────────────────────────────────────
// UI State
// ─────────────────────────────────────────

/**
 * Состояние экрана настроек.
 *
 * @param playerCount Выбранное число игроков (2..10).
 * @param isLoading   true пока DataStore не вернул первое значение.
 */
data class SettingsUiState(
    val playerCount: Int = 6,
    val isLoading: Boolean = true
)
