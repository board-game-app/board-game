package ru.internet.boardgames.spygame.data.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.spygame.data.local.datastore.SettingsDataStore
import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Реализация [SettingsRepository] из domain-слоя.
 *
 * Делегирует все операции в [SettingsDataStore],
 * скрывая детали DataStore от domain/presentation слоёв.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    /** Flow числа игроков (эмитит при каждом изменении). */
    override val playerCount: Flow<Int> = settingsDataStore.playerCount

    /** Flow кода языка: "ru", "en" или "system". */
    override val language: Flow<String> = settingsDataStore.language

    override suspend fun setPlayerCount(count: Int) {
        settingsDataStore.setPlayerCount(count)
    }

    override suspend fun setLanguage(language: String) {
        settingsDataStore.setLanguage(language)
    }
}
