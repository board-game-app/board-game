package ru.internet.boardgames.spygame.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность категории.
 * Хранит локализованное название и признак языка.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,         // Уникальный ID категории, напр. "airport"
    val name: String,                   // Локализованное название, напр. "Аэропорт"
    val language: String                // Код языка: "ru" или "en"
)