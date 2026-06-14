package ru.internet.boardgames.soundquiz.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.GameSettings

interface SoundQuizSettingsRepository {

    /** Поток настроек с дефолтными значениями при первом запуске */
    fun getSettings(): Flow<GameSettings>

    /** Атомарная запись всех настроек */
    suspend fun saveSettings(settings: GameSettings)
}
