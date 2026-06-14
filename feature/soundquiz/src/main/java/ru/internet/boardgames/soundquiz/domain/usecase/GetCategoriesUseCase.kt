package ru.internet.boardgames.soundquiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

/** Возвращает список категорий для указанного языка как реактивный поток */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(language: String): Flow<List<Category>> =
        repository.getCategories(language)
}
