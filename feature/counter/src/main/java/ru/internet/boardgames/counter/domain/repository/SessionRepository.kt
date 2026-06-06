package ru.internet.boardgames.counter.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.Session

/**
 * Интерфейс репозитория сессий.
 * Работает только с Room — DataStore-методы перенесены в [CounterPrefsRepository].
 */
interface SessionRepository {

    /** Реактивный поток всех сессий, от новых к старым */
    fun getAllSessions(): Flow<List<Session>>

    /** Реактивный поток одной сессии; null если не найдена */
    fun getSessionById(id: Long): Flow<Session?>

    /**
     * Одноразовое чтение всех сессий (не Flow).
     * Используется в [EnsureDefaultSessionUseCase] при старте.
     */
    suspend fun getAllSessionsOnce(): List<Session>

    /** Создать сессию, вернуть сгенерированный id */
    suspend fun createSession(name: String): Long

    /** Обновить сессию (например, переименовать) */
    suspend fun updateSession(session: Session)

    /**
     * Удалить сессию по id.
     * Счётчики удаляются каскадно через ForeignKey.CASCADE.
     */
    suspend fun deleteSession(sessionId: Long)
}
