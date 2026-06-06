package ru.internet.boardgames.counter.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.CardDisplayMode

/**
 * Интерфейс репозитория пользовательских предпочтений счётчика.
 * Отделён от [CounterRepository], так как хранится в DataStore, а не в Room.
 *
 * Включает:
 *   • режим отображения карточек (§5, §11 ТЗ — сохраняется между сессиями)
 *   • id последней активной сессии (перенесён из SessionRepository
 *     для логической группировки: оба ключа — в одном DataStore)
 */
interface CounterPrefsRepository {

    /** Реактивный поток режима отображения карточек */
    fun observeCardDisplayMode(): Flow<CardDisplayMode>

    /** Сохранить выбранный режим отображения карточек */
    suspend fun setCardDisplayMode(mode: CardDisplayMode)

    /** Реактивный поток id последней активной сессии (null — сессий не было) */
    fun observeLastActiveSessionId(): Flow<Long?>

    /** Запомнить, какую сессию пользователь открыл последней */
    suspend fun setLastActiveSessionId(sessionId: Long)
}
