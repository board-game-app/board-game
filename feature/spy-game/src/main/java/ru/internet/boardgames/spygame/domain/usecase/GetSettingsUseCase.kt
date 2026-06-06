package ru.internet.boardgames.spygame.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.spygame.domain.model.AppSettings
import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case чтения настроек приложения.
 *
 * Оборачивает Flow из репозитория в типизированный [AppSettings].
 * ViewModel подписывается на него через [invoke] и реагирует на изменения.
 *
 * Язык убран из настроек — он читается из системной локали в [GameViewModel].
 *
 * Используется в:
 * - [SettingsViewModel] — для отображения текущих настроек
 * - [GameViewModel] — для определения числа игроков при старте игры
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Возвращает холодный Flow, эмитящий актуальный [AppSettings]
     * при каждом изменении playerCount.
     */
    operator fun invoke(): Flow<AppSettings> =
        settingsRepository.playerCount.map { playerCount ->
            AppSettings(playerCount = playerCount)
        }
}
