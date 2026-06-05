package ru.internet.boardgames.spygame.data.model

import kotlinx.serialization.Serializable

/**
 * DTO одной категории из JSON-файла контента.
 * Пример: { "id": "airport", "name": "Аэропорт", "words": ["Пилот", ...] }
 */
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val words: List<String>
)

/**
 * Корневой объект файла categories_{lang}.json.
 */
@Serializable
data class CategoriesFileDto(
    val language: String,           // "ru" или "en"
    val version: Int,               // Должен совпадать с contentVersion манифеста
    val categories: List<CategoryDto>
)
