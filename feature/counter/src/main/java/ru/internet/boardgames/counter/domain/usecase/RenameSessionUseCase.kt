package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Переименовывает сессию.
 * Используется при inline-переименовании в [SessionDrawer].
 *
 * Не зависит от Android — только domain-слой.
 */
class RenameSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(session: Session, newName: String) {
        if (newName.isBlank()) return
        sessionRepository.updateSession(session.copy(name = newName.trim()))
    }
}
