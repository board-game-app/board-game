package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import javax.inject.Inject

/** Переключить активную сессию */
class SetActiveSessionUseCase @Inject constructor(
    private val prefs: CounterPrefsRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        prefs.setLastActiveSessionId(sessionId)
    }
}
