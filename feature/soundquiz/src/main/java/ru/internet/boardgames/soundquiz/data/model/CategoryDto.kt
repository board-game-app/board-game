package ru.internet.boardgames.soundquiz.data.model

import kotlinx.serialization.Serializable

/**
 * DTO одной категории из JSON-файла assets.
 * Слова хранятся как плоский список строк.
 */
@Serializable
data class CategoryDto(
    val name: String,
    val colorArgb: Int,
    val displayOrder: Int,
    val words: List<String>
)

/**
 * Корневой объект JSON-файла категорий (categories_sq_ru.json / categories_sq_en.json).
 */
@Serializable
data class CategoriesFileSq(
    val categories: List<CategoryDto>
)
