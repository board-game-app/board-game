package ru.internet.boardgames.soundquiz.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.usecase.CreateCategoryUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.DeleteCategoryUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.GetCategoriesUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.UpdateCategoryUseCase
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val createCategory: CreateCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    /** Язык из локали устройства — отображаем только категории текущей локали */
    private val currentLanguage: String =
        if (Locale.getDefault().language == "ru") "ru" else "en"

    init {
        viewModelScope.launch {
            getCategories(currentLanguage).collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    fun onShowCreateDialog() {
        _uiState.update { it.copy(dialogState = CategoryDialogState.Create()) }
    }

    fun onShowEditDialog(category: Category) {
        _uiState.update {
            it.copy(
                dialogState = CategoryDialogState.Edit(
                    originalCategory  = category,
                    name              = category.name,
                    selectedColorArgb = category.colorArgb
                )
            )
        }
    }

    fun onDialogDismiss() {
        _uiState.update { it.copy(dialogState = null) }
    }

    fun onDialogNameChanged(name: String) {
        _uiState.update { state ->
            val dialog = state.dialogState ?: return@update state
            state.copy(
                dialogState = when (dialog) {
                    is CategoryDialogState.Create -> dialog.copy(name = name)
                    is CategoryDialogState.Edit   -> dialog.copy(name = name)
                }
            )
        }
    }

    fun onDialogColorChanged(colorArgb: Int) {
        _uiState.update { state ->
            val dialog = state.dialogState ?: return@update state
            state.copy(
                dialogState = when (dialog) {
                    is CategoryDialogState.Create -> dialog.copy(selectedColorArgb = colorArgb)
                    is CategoryDialogState.Edit   -> dialog.copy(selectedColorArgb = colorArgb)
                }
            )
        }
    }

    fun onDialogConfirm() {
        viewModelScope.launch {
            when (val dialog = _uiState.value.dialogState) {
                is CategoryDialogState.Create -> {
                    val name = dialog.name.trim()
                    if (name.isNotEmpty()) {
                        // Язык берётся из локали — пользователь не выбирает
                        createCategory(name, dialog.selectedColorArgb, currentLanguage)
                    }
                }
                is CategoryDialogState.Edit -> {
                    val name = dialog.name.trim()
                    if (name.isNotEmpty()) {
                        updateCategory(dialog.originalCategory.copy(
                            name      = name,
                            colorArgb = dialog.selectedColorArgb
                        ))
                    }
                }
                null -> return@launch
            }
            _uiState.update { it.copy(dialogState = null) }
        }
    }

    fun onDeleteRequest(categoryId: Long) {
        _uiState.update { it.copy(deleteConfirmationId = categoryId) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(deleteConfirmationId = null) }
    }

    fun onDeleteConfirm() {
        val id = _uiState.value.deleteConfirmationId ?: return
        viewModelScope.launch {
            deleteCategory(id)
            _uiState.update { it.copy(deleteConfirmationId = null) }
        }
    }
}
