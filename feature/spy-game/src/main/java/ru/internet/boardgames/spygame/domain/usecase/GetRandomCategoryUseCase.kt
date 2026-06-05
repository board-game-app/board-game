package ru.internet.boardgames.spygame.domain.usecase

import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case получения случайной категории.
 *
 * Инкапсулирует обращение к [CategoryRepository] и логику языкового fallback:
 * если запрошенный язык не поддерживается — используется "en".
 */
class GetRandomCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    companion object {
        /** Языки, для которых есть контент. Должны совпадать с manifest.json. */
        private val SUPPORTED_LANGUAGES = setOf("ru", "en")
        private const val FALLBACK_LANGUAGE = "en"
    }

    /**
     * Возвращает случайную категорию.
     *
     * @param language  Запрошенный код языка (уже разрешённый: "ru" или "en",
     *                  без "system" — это делает ViewModel перед вызовом).
     * @param excludeId ID текущей категории, которую нужно исключить. null — без исключений.
     */
    suspend operator fun invoke(
        language: String,
        excludeId: String? = null
    ): Category {
        // Fallback на английский, если язык не поддерживается
        val resolvedLanguage = if (language in SUPPORTED_LANGUAGES) language else FALLBACK_LANGUAGE
        return categoryRepository.getRandomCategory(resolvedLanguage, excludeId)
    }
}
