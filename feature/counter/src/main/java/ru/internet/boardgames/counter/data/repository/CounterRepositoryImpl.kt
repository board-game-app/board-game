package ru.internet.boardgames.counter.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.counter.data.local.db.CounterDao
import ru.internet.boardgames.counter.data.local.db.CounterEntity
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepositoryImpl @Inject constructor(
    private val counterDao: CounterDao
) : CounterRepository {

    override fun getCountersBySession(sessionId: Long): Flow<List<Counter>> =
        counterDao.getCountersBySession(sessionId)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCounterById(counterId: Long): Counter? =
        counterDao.getCounterById(counterId)?.toDomain()

    override suspend fun createCounter(
        sessionId: Long,
        name: String,
        colorArgb: Long,
        incrementStep: Int,
        decrementStep: Int,
        initialValue: Int,
        resetValue: Int,
        actions: List<Int>
    ): Long {
        // Следующий порядок = максимальный + 1; 0 если список пуст
        val nextOrder = (counterDao.getMaxDisplayOrder(sessionId) ?: -1) + 1
        val entity = CounterEntity(
            sessionId    = sessionId,
            name         = name.trim(),
            value        = initialValue,
            resetValue   = resetValue,
            colorArgb    = colorArgb,
            incrementStep = incrementStep,
            decrementStep = decrementStep,
            actions      = actions.sorted(),      // §8 п.4: список хранится отсортированным
            displayOrder = nextOrder
        )
        return counterDao.insertCounter(entity)
    }

    override suspend fun updateCounter(counter: Counter) {
        counterDao.updateCounter(counter.toEntity())
    }

    override suspend fun applyDelta(counterId: Long, delta: Int) {
        counterDao.applyDelta(counterId, delta)
    }

    override suspend fun resetCounter(counterId: Long, resetValue: Int) {
        counterDao.resetCounter(counterId, resetValue)
    }

    override suspend fun deleteCounter(counterId: Long) {
        counterDao.deleteCounter(counterId)
    }

    // ── Маппинг Entity ↔ Domain ──────────────────────────────────────────────

    private fun CounterEntity.toDomain() = Counter(
        id            = id,
        sessionId     = sessionId,
        name          = name,
        value         = value,
        resetValue    = resetValue,
        colorArgb     = colorArgb,
        incrementStep = incrementStep,
        decrementStep = decrementStep,
        actions       = actions,        // TypeConverter уже вернул List<Int>
        displayOrder  = displayOrder
    )

    private fun Counter.toEntity() = CounterEntity(
        id            = id,
        sessionId     = sessionId,
        name          = name,
        value         = value,
        resetValue    = resetValue,
        colorArgb     = colorArgb,
        incrementStep = incrementStep,
        decrementStep = decrementStep,
        actions       = actions,
        displayOrder  = displayOrder
    )
}
