package ru.internet.boardgames.spygame.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория пользовательских настроек.
 * Реализуется в data-слое ([SettingsRepositoryImpl]) через DataStore.
 */
interface SettingsRepository {

    /** Горячий Flow числа игроков. Эмитит при каждом изменении. */
    val playerCount: Flow<Int>

    /**
     * Горячий Flow языкового кода.
     * Возможные значения: "ru", "en", "system".
     */
    val language: Flow<String>

    /** Сохраняет число игроков. [count] должен быть в диапазоне 2..10. */
    suspend fun setPlayerCount(count: Int)

    /** Сохраняет языковой код. [language] — одно из: "ru", "en", "system". */
    suspend fun setLanguage(language: String)
}
