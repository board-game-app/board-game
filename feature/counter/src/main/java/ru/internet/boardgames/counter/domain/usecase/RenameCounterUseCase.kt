package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Переименовать счётчик.
 * Операция игнорируется, если новое имя пустое или состоит из пробелов.
 */
class RenameCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    suspend operator fun invoke(counter: Counter, newName: String) {
        if (newName.isBlank()) return
        counterRepository.updateCounter(counter.copy(name = newName.trim()))
    }
}