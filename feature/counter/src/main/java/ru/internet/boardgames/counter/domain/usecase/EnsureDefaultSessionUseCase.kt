package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Проверяет, есть ли в базе хотя бы одна сессия.
 * Если список пуст — автоматически создаёт сессию с именем «Основная».
 *
 * Вызывается:
 *   1. При старте [CounterViewModel] — до запуска реактивных наблюдателей.
 *   2. После удаления сессии (если был удалён последний экземпляр).
 *
 * Не зависит от Android — только domain-слой.
 */
class EnsureDefaultSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val createSessionUseCase: CreateSessionUseCase
) {
    suspend operator fun invoke() {
        val sessions = sessionRepository.getAllSessionsOnce()
        if (sessions.isEmpty()) {
            createSessionUseCase("Основная")
        }
    }
}
