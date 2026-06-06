package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.data.local.db.CounterEntity
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Создать новый счётчик с полными параметрами.
 * Вызывается как из диалога быстрого создания (§6), так и из экрана
 * редактирования (§7) при создании нового счётчика.
 */
class CreateCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        name: String,
        colorArgb: Long = CounterEntity.DEFAULT_COLOR_ARGB,
        incrementStep: Int = 1,
        decrementStep: Int = 1,
        initialValue: Int = 0,
        resetValue: Int = 0,
        actions: List<Int> = emptyList()
    ): Long = counterRepository.createCounter(
        sessionId     = sessionId,
        name          = name,
        colorArgb     = colorArgb,
        incrementStep = incrementStep,
        decrementStep = decrementStep,
        initialValue = initialValue,
        resetValue    = resetValue,
        actions       = actions
    )
}
