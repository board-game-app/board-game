package ru.internet.boardgames.soundquiz.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Сущность слова в базе данных.
 * CASCADE DELETE: при удалении категории все её слова удаляются автоматически.
 */
@Entity(
    tableName = "sq_words",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryId: Long,
    val word: String,
    /** Язык слова: "ru", "en" или "" для пользовательских слов */
    val language: String
)
