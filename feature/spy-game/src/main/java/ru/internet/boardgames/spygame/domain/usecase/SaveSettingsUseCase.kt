package ru.internet.boardgames.spygame.domain.usecase

import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case сохранения настроек приложения.
 *
 * Содержит бизнес-правила валидации перед записью:
 * - playerCount ограничен диапазоном [MIN_PLAYERS]..[MAX_PLAYERS]
 *
 * Язык убран из настроек: он определяется системной локалью устройства.
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    companion object {
        const val MIN_PLAYERS = 2
        const val MAX_PLAYERS = 10
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
}
