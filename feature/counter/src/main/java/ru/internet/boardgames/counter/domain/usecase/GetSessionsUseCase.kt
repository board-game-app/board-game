package ru.internet.boardgames.counter.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/** Получить реактивный поток всех сессий */
class GetSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<List<Session>> =
        sessionRepository.getAllSessions()
}