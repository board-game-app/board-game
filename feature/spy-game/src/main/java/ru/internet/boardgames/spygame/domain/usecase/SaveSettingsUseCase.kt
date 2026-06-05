package ru.internet.boardgames.spygame.domain.usecase

import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case сохранения настроек приложения.
 *
 * Содержит бизнес-правила валидации перед записью:
 * - playerCount ограничен диапазоном [MIN_PLAYERS]..[MAX_PLAYERS]
 * - language должен быть одним из допустимых значений
 *
 * SettingsViewModel вызывает нужный метод при изменении каждой настройки.
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    companion object {
        const val MIN_PLAYERS = 2
        const val MAX_PLAYERS = 10
        val ALLOWED_LANGUAGES = setOf("ru", "en", "system")
    }

    /**
     * Сохраняет число игроков.
     * Значение зажимается в допустимый диапазон вместо выброса исключения —
     * это безопаснее для UI, где слайдер может передать граничное значение.
     *
     * @param count Желаемое число игроков.
     */
    suspend fun savePlayerCount(count: Int) {
        val clamped = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)
        settingsRepository.setPlayerCount(clamped)
    }

    /**
     * Сохраняет языковой код.
     * При неизвестном значении сохраняет "system" как безопасный fallback.
     *
     * @param language Код языка: "ru", "en" или "system".
     */
    suspend fun saveLanguage(language: String) {
        val safe = if (language in ALLOWED_LANGUAGES) language else "system"
        settingsRepository.setLanguage(safe)
    }
}
