package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

/** Обновляет название и цвет пользовательской категории */
class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) =
        repository.updateCategory(category)
}
