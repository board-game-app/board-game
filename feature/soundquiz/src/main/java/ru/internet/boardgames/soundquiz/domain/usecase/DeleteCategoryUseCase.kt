package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Удаляет пользовательскую категорию.
 * Встроенные категории (isBuiltIn = true) защищены на уровне SQL —
 * DELETE не применяется к ним даже если ID передан ошибочно.
 * CASCADE DELETE в Room автоматически удаляет все слова категории.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long) =
        repository.deleteCategory(categoryId)
}
