package ru.internet.boardgames.counter.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.Counter

interface CounterRepository {

    /** Реактивный поток счётчиков сессии, упорядоченных по displayOrder */
    fun getCountersBySession(sessionId: Long): Flow<List<Counter>>

    /** Одноразовое чтение одного счётчика по id; null если не найден */
    suspend fun getCounterById(counterId: Long): Counter?

    /** Создать счётчик с полными параметрами, вернуть id */
    suspend fun createCounter(
        sessionId: Long,
        name: String,
        colorArgb: Long = 0xFFF5A623L,
        incrementStep: Int = 1,
        decrementStep: Int = 1,
        initialValue: Int = 0,
        resetValue: Int = 0,
        actions: List<Int> = emptyList()
    ): Long

    /** Обновить счётчик целиком (имя, цвет, шаги, сброс, действия, порядок) */
    suspend fun updateCounter(counter: Counter)

    /**
     * Атомарно изменить значение на delta.
     * Покрывает инкремент (+step), декремент (-step) и быстрые действия (+N/-N).
     */
    suspend fun applyDelta(counterId: Long, delta: Int)

    /** Сбросить значение счётчика до его resetValue */
    suspend fun resetCounter(counterId: Long, resetValue: Int)

    /** Удалить счётчик */
    suspend fun deleteCounter(counterId: Long)
}
