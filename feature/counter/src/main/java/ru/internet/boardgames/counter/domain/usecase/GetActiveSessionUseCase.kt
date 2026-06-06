package ru.internet.boardgames.counter.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Получить реактивный поток активной сессии.
 * lastActiveSessionId читается из [CounterPrefsRepository] (DataStore),
 * сама сессия — из [SessionRepository] (Room).
 */
class GetActiveSessionUseCase @Inject constructor(
    private val prefs: CounterPrefsRepository,
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Session?> =
        prefs.observeLastActiveSessionId()
            .flatMapLatest { sessionId ->
                if (sessionId == null) flowOf(null)
                else sessionRepository.getSessionById(sessionId)
            }
}
