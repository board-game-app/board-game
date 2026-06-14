package ru.internet.boardgames.soundquiz.domain.model

/**
 * Настройки игры, хранящиеся в DataStore.
 * Язык контента определяется автоматически из локали устройства —
 * явный выбор языка пользователем убран.
 *
 * numberOfCategories — количество стопок (1..5, default 3).
 * wordsPerCategory   — слов в каждой стопке (1..10, default 5).
 * timerSeconds       — длительность таймера (5..300, default 60).
 * pinnedCategoryIds  — ID зафиксированных категорий.
 */
data class GameSettings(
    val numberOfCategories: Int = 3,
    val wordsPerCategory: Int = 5,
    val timerSeconds: Int = 60,
    val pinnedCategoryIds: Set<Long> = emptySet()
)
