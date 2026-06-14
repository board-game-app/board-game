package ru.internet.boardgames.soundquiz.domain.usecase

import kotlinx.coroutines.flow.first
import ru.internet.boardgames.soundquiz.domain.model.CardStack
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.model.GameSession
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import ru.internet.boardgames.soundquiz.domain.repository.SoundQuizSettingsRepository
import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import java.util.Locale
import javax.inject.Inject

/**
 * Генерирует новую игровую сессию по текущим настройкам.
 * Язык контента всегда берётся из локали устройства (ru → "ru", всё остальное → "en").
 * Цвета стопок назначаются из перемешанной палитры — дубликатов нет.
 */
class GenerateGameSessionUseCase @Inject constructor(
    private val settingsRepository: SoundQuizSettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val wordRepository: WordRepository
) {
    companion object {
        /**
         * Предопределённые ARGB-цвета стопок (8 вариантов).
         * Совпадают с CardPaletteColors в Color.kt, дублированы здесь
         * чтобы domain-слой не зависел от presentation-слоя.
         */
        private val COLOR_PALETTE = listOf(
            -1754827,  // Красный   0xFFE53935
            -291840,   // Оранжевый 0xFFFB8C00
            -141259,   // Жёлтый   0xFFFDD835
            -12345273, // Зелёный  0xFF43A047
            -16540699, // Голубой  0xFF039BE5
            -14776091, // Синий    0xFF1E88E5
            -7461718,  // Фиолет.  0xFF8E24AA
            -2614432   // Розовый  0xFFD81B60
        )
    }
    suspend operator fun invoke(): GameSession {
        val settings     = settingsRepository.getSettings().first()
        val languageCode = resolveLanguageCode()
        val N            = settings.numberOfCategories

        val pinnedCategories: List<Category> =
            if (settings.pinnedCategoryIds.isNotEmpty()) {
                categoryRepository.getCategoriesByIds(settings.pinnedCategoryIds)
                    .filter { it.language == languageCode }
            } else emptyList()

        val selectedCategories: List<Category> = if (pinnedCategories.size < N) {
            val needed    = N - pinnedCategories.size
            val pinnedIds = pinnedCategories.map { it.id }.toSet()
            val available = categoryRepository.getCategories(languageCode).first()
                .filter { it.id !in pinnedIds }
                .shuffled()
            pinnedCategories + available.take(needed)
        } else {
            pinnedCategories.take(N)
        }

        val categoryIds     = selectedCategories.map { it.id }
        val allWords        = wordRepository.getWordsByCategoryIds(categoryIds)
        val wordsByCategory = allWords.groupBy { it.categoryId }

        // Перемешиваем палитру один раз для всей сессии —
        // каждая стопка получает уникальный цвет независимо от цвета категории в БД.
        // При N ≤ 8 (максимум 5 по настройкам) дубликатов никогда не будет.
        val shuffledColors = COLOR_PALETTE.shuffled()

        val stacks = selectedCategories.mapIndexed { index, category ->
            val selected = (wordsByCategory[category.id] ?: emptyList())
                .shuffled()
                .take(settings.wordsPerCategory)
            CardStack(
                category  = category,
                colorArgb = shuffledColors[index],
                words     = selected,
                explained = emptyList()
            )
        }

        return GameSession(stacks = stacks, activeCard = null, timerSeconds = settings.timerSeconds)
    }

    /** ru → "ru", всё остальное → "en" */
    fun resolveLanguageCode(): String =
        if (Locale.getDefault().language == "ru") "ru" else "en"
}
