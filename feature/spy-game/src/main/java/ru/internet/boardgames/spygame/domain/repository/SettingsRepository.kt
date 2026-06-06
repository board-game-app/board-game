package ru.internet.boardgames.spygame.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория пользовательских настроек.
 * Реализуется в data-слое ([SettingsRepositoryImpl]) через DataStore.
 *
 * Язык убран из настроек: он определяется системной локалью устройства.
 */
interface SettingsRepository {

    /** Горячий Flow числа игроков. Эмитит при каждом изменении. */
    val playerCount: Flow<Int>

    /** Сохраняет число игроков. [count] должен быть в диапазоне 2..10. */
    suspend fun setPlayerCount(count: Int)
}
