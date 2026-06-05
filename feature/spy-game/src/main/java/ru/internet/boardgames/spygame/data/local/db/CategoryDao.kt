package ru.internet.boardgames.spygame.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {

    /**
     * Возвращает все категории для заданного языка.
     * Используется при отладке и в тестах.
     */
    @Query("SELECT * FROM categories WHERE language = :language")
    suspend fun getCategoriesByLanguage(language: String): List<CategoryEntity>

    /**
     * Случайная категория для языка (без исключений).
     * Используется когда [excludeId] = null.
     */
    @Query("SELECT * FROM categories WHERE language = :language ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCategory(language: String): CategoryEntity?

    /**
     * Случайная категория, исключая [excludeId].
     * Используется при «Обновить категорию», чтобы не повторять текущую.
     */
    @Query("SELECT * FROM categories WHERE language = :language AND id != :excludeId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCategoryExcluding(language: String, excludeId: String): CategoryEntity?

    /**
     * Вставка списка категорий. REPLACE — перезаписывает при конфликте первичного ключа.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    /**
     * Полная очистка таблицы при пересеивании контента.
     * Так как у WordEntity есть ForeignKey с CASCADE,
     * слова удалятся автоматически через удаление категорий.
     * Дополнительного вызова wordDao.deleteAll() не требуется,
     * но мы всё равно вызываем его явно для надёжности.
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
