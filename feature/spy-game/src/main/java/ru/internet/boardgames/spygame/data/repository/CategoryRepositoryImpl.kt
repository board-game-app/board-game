package ru.internet.boardgames.spygame.data.repository

import ru.internet.boardgames.spygame.data.local.db.CategoryDao
import ru.internet.boardgames.spygame.data.local.db.WordDao
import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Реализация [CategoryRepository] из domain-слоя.
 *
 * Инкапсулирует работу с Room: достаёт CategoryEntity + WordEntity
 * и собирает доменную модель [Category].
 *
 * Примечание: импорты из domain-слоя появятся после создания
 * файлов Шага 2. До тех пор проект не будет компилироваться —
 * это ожидаемое поведение при послойной сборке.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val wordDao: WordDao
) : CategoryRepository {

    /**
     * Возвращает случайную категорию для заданного языка.
     *
     * @param language код языка: "ru" или "en"
     * @param excludeId ID категории, которую нужно исключить (текущая категория при refresh)
     * @throws IllegalStateException если в базе нет категорий для данного языка
     */
    override suspend fun getRandomCategory(
        language: String,
        excludeId: String?
    ): Category {
        val entity = if (excludeId != null) {
            // Пробуем найти категорию кроме текущей
            categoryDao.getRandomCategoryExcluding(language, excludeId)
            // Если осталась только одна категория — fallback без исключения
                ?: categoryDao.getRandomCategory(language)
        } else {
            categoryDao.getRandomCategory(language)
        } ?: throw IllegalStateException(
            "Нет категорий для языка '$language'. Убедитесь, что ContentLoader выполнил seeding."
        )

        val words = wordDao.getWordsByCategoryAndLanguage(entity.id, language)

        return Category(
            id = entity.id,
            name = entity.name,
            words = words.map { it.word }
        )
    }

    /**
     * Возвращает все категории для заданного языка.
     * Используется в тестах и потенциальном экране списка категорий.
     */
    override suspend fun getCategories(language: String): List<Category> {
        val entities = categoryDao.getCategoriesByLanguage(language)
        return entities.map { entity ->
            val words = wordDao.getWordsByCategoryAndLanguage(entity.id, language)
            Category(
                id = entity.id,
                name = entity.name,
                words = words.map { it.word }
            )
        }
    }
}