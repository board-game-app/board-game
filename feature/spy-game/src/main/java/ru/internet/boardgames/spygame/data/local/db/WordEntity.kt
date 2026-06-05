package ru.internet.boardgames.spygame.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-сущность слова, принадлежащего категории.
 *
 * ForeignKey с onDelete = CASCADE: при удалении категории
 * все её слова удаляются автоматически.
 *
 * Индекс по [categoryId] ускоряет выборку слов по категории.
 */
@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["categoryId", "language"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val wordId: Long = 0,
    val categoryId: String,             // Ссылка на CategoryEntity.id
    val word: String,                   // Само слово, напр. "Пилот"
    val language: String                // Код языка: "ru" или "en"
)
