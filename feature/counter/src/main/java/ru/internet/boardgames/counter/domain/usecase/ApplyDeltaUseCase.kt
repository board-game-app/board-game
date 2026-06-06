package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Атомарно изменить значение счётчика на [delta].
 *
 * Единый use case вместо отдельных Increment/Decrement/ApplyAction:
 *   • кнопка «+»        → delta = +incrementStep
 *   • кнопка «−»        → delta = -decrementStep
 *   • кнопка действия   → delta = action.value (§5.3)
 *
 * delta = 0 игнорируется.
 */
class ApplyDeltaUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(counterId: Long, delta: Int) {
        if (delta == 0) return
        counterRepository.applyDelta(counterId, delta)
    }
}
