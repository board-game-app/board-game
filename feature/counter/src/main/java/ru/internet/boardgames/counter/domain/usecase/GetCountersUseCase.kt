package ru.internet.boardgames.counter.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject

/** Получить реактивный поток счётчиков для указанной сессии */
class GetCountersUseCase @Inject constructor(
    private val counterRepository: CounterRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<Counter>> =
        counterRepository.getCountersBySession(sessionId)
}