package ru.internet.boardgames.soundquiz.presentation.category

import ru.internet.boardgames.soundquiz.domain.model.Word

/**
 * UI-состояние экрана управления словами категории.
 * isBuiltIn — управляет видимостью кнопок CRUD.
 * dialogState != null — открыт диалог добавления или редактирования слова.
 */
data class WordManagementUiState(
    val categoryName: String = "",
    val isBuiltIn: Boolean = false,
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val dialogState: WordDialogState? = null
)

/** Состояние диалога добавления/редактирования слова */
sealed class WordDialogState {
    data class Add(val text: String = "") : WordDialogState()
    data class Edit(val word: Word, val text: String) : WordDialogState()
}
