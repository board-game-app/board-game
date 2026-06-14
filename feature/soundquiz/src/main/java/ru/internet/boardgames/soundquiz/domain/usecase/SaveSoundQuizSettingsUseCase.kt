package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.model.GameSettings
import ru.internet.boardgames.soundquiz.domain.repository.SoundQuizSettingsRepository
import javax.inject.Inject

/** Атомарно сохраняет все настройки игры в DataStore */
class SaveSoundQuizSettingsUseCase @Inject constructor(
    private val repository: SoundQuizSettingsRepository
) {
    suspend operator fun invoke(settings: GameSettings) =
        repository.saveSettings(settings)
}
