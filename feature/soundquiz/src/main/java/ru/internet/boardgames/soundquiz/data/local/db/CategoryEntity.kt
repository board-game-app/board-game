package ru.internet.boardgames.soundquiz.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность категории в базе данных.
 * isBuiltIn = true — встроенные (из assets), нельзя удалять и редактировать.
 */
@Entity(tableName = "sq_categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    /** Цвет стопки в формате ARGB */
    val colorArgb: Int,
    /** Язык категории: "ru" или "en" */
    val language: String,
    /** true — встроенная (из assets), false — пользовательская */
    val isBuiltIn: Boolean,
    /** Порядок отображения в списке */
    val displayOrder: Int
)
