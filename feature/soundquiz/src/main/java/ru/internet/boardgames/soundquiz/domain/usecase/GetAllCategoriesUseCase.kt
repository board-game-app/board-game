package ru.internet.boardgames.soundquiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Возвращает все категории обоих языков — используется в CategoryManagementScreen,
 * где пользователь управляет контентом независимо от текущего языка игры.
 */
class GetAllCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> =
        repository.getAllCategories()
}
