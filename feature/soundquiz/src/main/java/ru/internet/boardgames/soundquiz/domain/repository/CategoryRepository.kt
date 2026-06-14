package ru.internet.boardgames.soundquiz.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.Category

interface CategoryRepository {

    /** Категории конкретного языка — для игры и экрана настроек */
    fun getCategories(language: String): Flow<List<Category>>

    /** Все категории обоих языков — для экрана управления категориями */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Категории по набору ID — для получения pinned-категорий.
     * Фильтрацию по языку выполняет вызывающий код (GenerateGameSessionUseCase).
     */
    suspend fun getCategoriesByIds(ids: Set<Long>): List<Category>

    /** Создание пользовательской категории; возвращает сгенерированный ID */
    suspend fun createCategory(name: String, colorArgb: Int, language: String): Long

    /** Обновление только редактируемых полей (name, colorArgb) */
    suspend fun updateCategory(category: Category)

    /** Удаление пользовательской категории; встроенные защищены на уровне SQL */
    suspend fun deleteCategory(categoryId: Long)
}
