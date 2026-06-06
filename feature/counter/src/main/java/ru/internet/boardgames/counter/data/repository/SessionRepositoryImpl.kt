package ru.internet.boardgames.counter.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.counter.data.local.db.SessionDao
import ru.internet.boardgames.counter.data.local.db.SessionEntity
import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> =
        sessionDao.getAllSessions()
            .map { entities -> entities.map { it.toDomain() } }

    override fun getSessionById(id: Long): Flow<Session?> =
        sessionDao.getSessionById(id)
            .map { it?.toDomain() }

    override suspend fun getAllSessionsOnce(): List<Session> =
        sessionDao.getAllSessionsOnce().map { it.toDomain() }

    override suspend fun createSession(name: String): Long {
        val entity = SessionEntity(
            name = name.trim(),
            createdAt = System.currentTimeMillis()
        )
        return sessionDao.insertSession(entity)
    }

    override suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session.toEntity())
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSession(sessionId)
    }

    // ── Маппинг ───────────────────────────────────────────────────────────────

    private fun SessionEntity.toDomain() = Session(
        id = id,
        name = name,
        createdAt = createdAt
    )

    private fun Session.toEntity() = SessionEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}
