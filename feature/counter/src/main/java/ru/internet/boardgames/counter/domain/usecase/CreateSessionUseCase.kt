package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Создать новую сессию и немедленно сделать её активной.
 */
class CreateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val prefs: CounterPrefsRepository
) {
    suspend operator fun invoke(name: String): Long {
        val sessionId = sessionRepository.createSession(name)
        prefs.setLastActiveSessionId(sessionId)
        return sessionId
    }
}
