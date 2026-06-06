package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Удалить счётчик.
 * Вызывается после подтверждения пользователем диалога «Удалить счётчик "Имя"?» (§11 ТЗ).
 */
class DeleteCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.deleteCounter(counterId)
    }
}
