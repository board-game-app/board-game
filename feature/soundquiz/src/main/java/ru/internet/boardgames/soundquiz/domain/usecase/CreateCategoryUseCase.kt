package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

/** Создаёт пользовательскую категорию; возвращает ID новой записи */
class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(name: String, colorArgb: Int, language: String): Long =
        repository.createCategory(name, colorArgb, language)
}
