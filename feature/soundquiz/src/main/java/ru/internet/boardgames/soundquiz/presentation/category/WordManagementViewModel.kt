package ru.internet.boardgames.soundquiz.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.internet.boardgames.soundquiz.domain.model.Word
import ru.internet.boardgames.soundquiz.domain.usecase.AddWordUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.DeleteWordUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.GetAllCategoriesUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.GetCategoryWordsUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.UpdateWordUseCase
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val getCategoryWords: GetCategoryWordsUseCase,
    private val addWord: AddWordUseCase,
    private val updateWord: UpdateWordUseCase,
    private val deleteWord: DeleteWordUseCase
) : ViewModel() {

    /**
     * categoryId передаётся через NavGraph как Long-аргумент.
     * NavGraph объявляет navArgument("categoryId") { type = NavType.LongType }.
     */
    private val categoryId: Long = checkNotNull(savedStateHandle["categoryId"])

    private val _uiState = MutableStateFlow(WordManagementUiState())
    val uiState: StateFlow<WordManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Находим категорию → подписываемся на её слова через flatMapLatest
            getAllCategories()
                .mapNotNull { list -> list.find { it.id == categoryId } }
                .flatMapLatest { category ->
                    getCategoryWords(category.id).map { words -> category to words }
                }
                .collect { (category, words) ->
                    _uiState.update { state ->
                        state.copy(
                            categoryName = category.name,
                            isBuiltIn = category.isBuiltIn,
                            words = words,
                            isLoading = false
                        )
                    }
                }
        }
    }

    // ── Диалог добавления ────────────────────────────────────────────────────

    fun onShowAddDialog() {
        _uiState.update { it.copy(dialogState = WordDialogState.Add()) }
    }

    // ── Диалог редактирования ────────────────────────────────────────────────

    fun onShowEditDialog(word: Word) {
        _uiState.update { it.copy(dialogState = WordDialogState.Edit(word, word.word)) }
    }

    // ── Общие методы диалога ─────────────────────────────────────────────────

    fun onDialogDismiss() {
        _uiState.update { it.copy(dialogState = null) }
    }

    fun onDialogTextChanged(text: String) {
        _uiState.update { state ->
            val dialog = state.dialogState ?: return@update state
            state.copy(
                dialogState = when (dialog) {
                    is WordDialogState.Add -> dialog.copy(text = text)
                    is WordDialogState.Edit -> dialog.copy(text = text)
                }
            )
        }
    }

    /** Подтверждение диалога: добавляет или обновляет слово */
    fun onDialogConfirm() {
        viewModelScope.launch {
            when (val dialog = _uiState.value.dialogState) {
                is WordDialogState.Add -> {
                    val text = dialog.text.trim()
                    if (text.isNotEmpty()) addWord(categoryId, text)
                }
                is WordDialogState.Edit -> {
                    val text = dialog.text.trim()
                    if (text.isNotEmpty()) updateWord(dialog.word.copy(word = text))
                }
                null -> return@launch
            }
            _uiState.update { it.copy(dialogState = null) }
        }
    }

    // ── Удаление слова ───────────────────────────────────────────────────────

    /** Удаление без подтверждения — пользователь может добавить слово снова */
    fun onDeleteWord(wordId: Long) {
        viewModelScope.launch { deleteWord(wordId) }
    }
}
