package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Сбросить значение счётчика до его [Counter.resetValue].
 * Вызывается после подтверждения пользователем диалога «Сбросить до N?» (§5.4).
 *
 * [resetValue] передаётся явно — ViewModel уже знает его из UiState,
 * нет необходимости делать дополнительный запрос к БД.
 */
class ResetCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(counterId: Long, resetValue: Int) {
        counterRepository.resetCounter(counterId, resetValue)
    }
}
