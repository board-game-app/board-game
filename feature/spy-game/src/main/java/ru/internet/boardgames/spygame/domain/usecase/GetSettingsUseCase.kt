package ru.internet.boardgames.spygame.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.internet.boardgames.spygame.domain.model.AppSettings
import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case чтения настроек приложения.
 *
 * Объединяет два независимых Flow из репозитория в один типизированный
 * [AppSettings]. ViewModel подписывается на него через [invoke] и
 * реагирует на любое изменение настроек.
 *
 * Используется в:
 * - [SettingsViewModel] — для отображения текущих настроек
 * - [GameViewModel] — для определения языка и числа игроков при старте игры
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Возвращает холодный Flow, эмитящий актуальный [AppSettings]
     * при каждом изменении playerCount или language.
     *
     * [combine] гарантирует, что первый эмит происходит только когда
     * оба upstream-а выдали хотя бы одно значение.
     */
    operator fun invoke(): Flow<AppSettings> = combine(
        settingsRepository.playerCount,
        settingsRepository.language
    ) { playerCount, language ->
        AppSettings(playerCount = playerCount, language = language)
    }
}
