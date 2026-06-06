package ru.internet.boardgames.counter.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    /** Реактивный поток счётчиков сессии, упорядоченных по displayOrder */
    @Query("SELECT * FROM counters WHERE sessionId = :sessionId ORDER BY displayOrder ASC")
    fun getCountersBySession(sessionId: Long): Flow<List<CounterEntity>>

    /** Одноразовое чтение одного счётчика (для сброса, редактирования) */
    @Query("SELECT * FROM counters WHERE id = :counterId")
    suspend fun getCounterById(counterId: Long): CounterEntity?

    /** Вставить счётчик, вернуть сгенерированный id */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCounter(counter: CounterEntity): Long

    /** Полное обновление счётчика (имя, цвет, шаги, сброс, действия и т.д.) */
    @Update
    suspend fun updateCounter(counter: CounterEntity)

    /**
     * Атомарное изменение значения на произвольную дельту.
     * Покрывает: инкремент (+step), декремент (-step), быстрое действие (+N / -N).
     * Единый SQL вместо трёх отдельных методов — исключает гонку при быстрых нажатиях.
     */
    @Query("UPDATE counters SET value = value + :delta WHERE id = :counterId")
    suspend fun applyDelta(counterId: Long, delta: Int)

    /**
     * Сброс значения до заданного числа.
     * Вызывается после подтверждения пользователем диалога (§5.4).
     */
    @Query("UPDATE counters SET value = :resetValue WHERE id = :counterId")
    suspend fun resetCounter(counterId: Long, resetValue: Int)

    /** Получить максимальный displayOrder в сессии (null если счётчиков нет) */
    @Query("SELECT MAX(displayOrder) FROM counters WHERE sessionId = :sessionId")
    suspend fun getMaxDisplayOrder(sessionId: Long): Int?

    /** Удалить счётчик по id */
    @Query("DELETE FROM counters WHERE id = :counterId")
    suspend fun deleteCounter(counterId: Long)
}
