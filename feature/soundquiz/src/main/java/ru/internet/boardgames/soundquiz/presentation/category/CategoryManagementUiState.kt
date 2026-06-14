package ru.internet.boardgames.soundquiz.presentation.category

import ru.internet.boardgames.soundquiz.domain.model.Category

data class CategoryManagementUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val dialogState: CategoryDialogState? = null,
    val deleteConfirmationId: Long? = null
)

sealed class CategoryDialogState {
    /** Язык новой категории определяется из локали — пользователь не выбирает */
    data class Create(
        val name: String = "",
        val selectedColorArgb: Int = -1754827  // CardPaletteColors[0] — Красный
    ) : CategoryDialogState()

    data class Edit(
        val originalCategory: Category,
        val name: String,
        val selectedColorArgb: Int
    ) : CategoryDialogState()
}
