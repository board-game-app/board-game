package ru.internet.boardgames.soundquiz.data.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.data.local.datastore.SoundQuizSettingsDataStore
import ru.internet.boardgames.soundquiz.domain.repository.SoundQuizSettingsRepository
import javax.inject.Inject
import ru.internet.boardgames.soundquiz.domain.model.GameSettings

class SoundQuizSettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SoundQuizSettingsDataStore
) : SoundQuizSettingsRepository {

    /** Поток настроек игры — подписывается ViewModel через use case */
    override fun getSettings(): Flow<GameSettings> = settingsDataStore.settingsFlow

    /** Атомарное сохранение всех настроек */
    override suspend fun saveSettings(settings: GameSettings) {
        settingsDataStore.saveSettings(settings)
    }
}