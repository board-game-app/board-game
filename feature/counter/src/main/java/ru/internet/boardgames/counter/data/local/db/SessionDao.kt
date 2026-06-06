package ru.internet.boardgames.counter.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    /** Реактивный поток всех сессий, отсортированных от новых к старым */
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    /** Реактивный поток одной сессии по id (null если не найдена) */
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<SessionEntity?>

    /** Одноразовое чтение сессии (suspend, не Flow) — нужно в use cases */
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionByIdOnce(sessionId: Long): SessionEntity?

    /**
     * Одноразовое чтение всех сессий (suspend, не Flow).
     * Нужно в EnsureDefaultSessionUseCase для проверки пустого списка при старте.
     */
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    /** Вставить сессию, вернуть сгенерированный id */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity): Long

    /** Обновить существующую сессию (например, переименовать) */
    @Update
    suspend fun updateSession(session: SessionEntity)

    /**
     * Удалить сессию по id.
     * Все счётчики каскадно удаляются через ForeignKey.CASCADE — дополнительной логики не нужно.
     */
    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
