package ru.internet.boardgames.counter.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность игровой сессии.
 * Сессии никогда не удаляются — пользователь хочет видеть историю.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Название сессии, заданное пользователем */
    val name: String,
    /** Время создания в миллисекундах (System.currentTimeMillis) */
    val createdAt: Long
)