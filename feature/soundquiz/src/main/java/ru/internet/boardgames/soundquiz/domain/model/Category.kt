package ru.internet.boardgames.soundquiz.domain.model

/**
 * Доменная модель категории.
 * wordCount вычисляется на уровне репозитория через JOIN-запрос.
 * language добавлен относительно спецификации: необходим для фильтрации
 * pinned-категорий по языку в GenerateGameSessionUseCase.
 */
data class Category(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val isBuiltIn: Boolean,
    val wordCount: Int,
    val language: String
)
