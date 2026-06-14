package ru.internet.boardgames.soundquiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.GameSettings
import ru.internet.boardgames.soundquiz.domain.repository.SoundQuizSettingsRepository
import javax.inject.Inject

/** Возвращает поток настроек игры; дефолтные значения применяются при первом запуске */
class GetSoundQuizSettingsUseCase @Inject constructor(
    private val repository: SoundQuizSettingsRepository
) {
    operator fun invoke(): Flow<GameSettings> =
        repository.getSettings()
}
