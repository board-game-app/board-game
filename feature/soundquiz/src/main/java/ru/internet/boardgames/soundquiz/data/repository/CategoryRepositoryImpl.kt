package ru.internet.boardgames.soundquiz.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.soundquiz.data.local.db.CategoryDao
import ru.internet.boardgames.soundquiz.data.local.db.CategoryEntity
import ru.internet.boardgames.soundquiz.data.local.db.CategoryWithWordCount
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(language: String): Flow<List<Category>> =
        categoryDao.getCategoriesWithWordCount(language).map { it.map(CategoryWithWordCount::toDomain) }

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategoriesWithWordCount().map { it.map(CategoryWithWordCount::toDomain) }

    override suspend fun getCategoriesByIds(ids: Set<Long>): List<Category> =
        categoryDao.getCategoriesWithWordCountByIds(ids).map(CategoryWithWordCount::toDomain)

    override suspend fun createCategory(name: String, colorArgb: Int, language: String): Long =
        categoryDao.insert(
            CategoryEntity(
                name = name, colorArgb = colorArgb, language = language,
                isBuiltIn = false, displayOrder = Int.MAX_VALUE
            )
        )

    override suspend fun updateCategory(category: Category) {
        val existing = categoryDao.getById(category.id) ?: return
        categoryDao.update(existing.copy(name = category.name, colorArgb = category.colorArgb))
    }

    /** Удаление категории — без ограничений на isBuiltIn */
    override suspend fun deleteCategory(categoryId: Long) {
        categoryDao.deleteCategory(categoryId)
    }
}

private fun CategoryWithWordCount.toDomain() = Category(
    id        = category.id,
    name      = category.name,
    colorArgb = category.colorArgb,
    isBuiltIn = category.isBuiltIn,
    wordCount = wordCount,
    language  = category.language
)
