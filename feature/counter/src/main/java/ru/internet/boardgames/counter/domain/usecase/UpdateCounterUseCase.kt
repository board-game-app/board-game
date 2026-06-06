package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Полное обновление счётчика (имя, цвет, шаги, сброс, действия).
 * Вызывается при сохранении из Экрана редактирования (§7, кнопка «✓»).
 *
 * Валидация:
 *   • имя не может быть пустым → игнорируем вызов с пустым именем.
 *   • incrementStep / decrementStep ≥ 1 → защита от некорректного ввода.
 *   • actions сортируются перед сохранением (§8 п.4).
 */
class UpdateCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(counter: Counter) {
        if (counter.name.isBlank()) return
        val sanitized = counter.copy(
            name          = counter.name.trim(),
            incrementStep = counter.incrementStep.coerceAtLeast(1),
            decrementStep = counter.decrementStep.coerceAtLeast(1),
            actions       = counter.actions.sorted()
        )
        counterRepository.updateCounter(sanitized)
    }
}
