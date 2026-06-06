package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Удаляет сессию из базы данных по её id.
 *
 * Все счётчики сессии удаляются каскадно через ForeignKey.CASCADE —
 * дополнительной логики в use case не требуется.
 *
 * Логика переключения активной сессии после удаления находится в
 * [CounterViewModel.confirmDeleteSession], так как требует знания текущего UI-состояния.
 *
 * Не зависит от Android — только domain-слой.
 */
class DeleteSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) {
        sessionRepository.deleteSession(sessionId)
    }
}
