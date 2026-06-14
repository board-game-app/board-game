package ru.internet.boardgames.soundquiz.presentation.settings

import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.model.GameSettings

/**
 * UI-состояние экрана настроек.
 * Является единственным источником правды для SettingsScreen.
 */
data class SettingsUiState(
    val settings: GameSettings = GameSettings(),
    /** Категории для текущего языка — для секции «Категории» */
    val categories: List<Category> = emptyList(),
    val categorySearchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** Список категорий с применённым фильтром поиска */
    val filteredCategories: List<Category>
        get() = if (categorySearchQuery.isBlank()) categories
                else categories.filter {
                    it.name.contains(categorySearchQuery, ignoreCase = true)
                }

    /** true — выбрано максимально допустимое число pinned-категорий */
    val isPinnedLimitReached: Boolean
        get() = settings.pinnedCategoryIds.size >= settings.numberOfCategories
}
