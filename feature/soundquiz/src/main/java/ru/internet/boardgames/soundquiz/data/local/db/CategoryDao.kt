package ru.internet.boardgames.soundquiz.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryWithWordCount(
    @Embedded val category: CategoryEntity,
    @ColumnInfo(name = "word_count") val wordCount: Int
)

@Dao
interface CategoryDao {

    @Query("""
        SELECT c.*, COUNT(w.id) AS word_count
        FROM sq_categories c
        LEFT JOIN sq_words w ON c.id = w.categoryId
        WHERE c.language = :language
        GROUP BY c.id
        ORDER BY c.displayOrder ASC
    """)
    fun getCategoriesWithWordCount(language: String): Flow<List<CategoryWithWordCount>>

    @Query("""
        SELECT c.*, COUNT(w.id) AS word_count
        FROM sq_categories c
        LEFT JOIN sq_words w ON c.id = w.categoryId
        GROUP BY c.id
        ORDER BY c.displayOrder ASC
    """)
    fun getAllCategoriesWithWordCount(): Flow<List<CategoryWithWordCount>>

    @Query("""
        SELECT c.*, COUNT(w.id) AS word_count
        FROM sq_categories c
        LEFT JOIN sq_words w ON c.id = w.categoryId
        WHERE c.id IN (:ids)
        GROUP BY c.id
    """)
    suspend fun getCategoriesWithWordCountByIds(ids: Collection<Long>): List<CategoryWithWordCount>

    @Query("SELECT * FROM sq_categories WHERE language = :language ORDER BY displayOrder ASC")
    suspend fun getCategoriesByLanguage(language: String): List<CategoryEntity>

    @Query("SELECT * FROM sq_categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * Удаление категории без ограничения по isBuiltIn —
     * пользователь может удалить любую категорию, включая встроенные.
     * CASCADE DELETE автоматически удаляет связанные слова.
     */
    @Query("DELETE FROM sq_categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    /** Используется ContentLoader при обновлении встроенного контента */
    @Query("DELETE FROM sq_categories WHERE isBuiltIn = 1 AND language = :language")
    suspend fun deleteBuiltInByLanguage(language: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>
}
