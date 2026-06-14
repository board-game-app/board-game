package ru.internet.boardgames.soundquiz.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    /** Слова категории как реактивный поток — для WordManagementScreen */
    @Query("SELECT * FROM sq_words WHERE categoryId = :categoryId ORDER BY id ASC")
    fun getByCategory(categoryId: Long): Flow<List<WordEntity>>

    /** Слова категории как одноразовый список — для генерации сессии */
    @Query("SELECT * FROM sq_words WHERE categoryId = :categoryId ORDER BY id ASC")
    suspend fun getByCategorySync(categoryId: Long): List<WordEntity>

    /** Слова нескольких категорий — для GenerateGameSessionUseCase */
    @Query("SELECT * FROM sq_words WHERE categoryId IN (:categoryIds)")
    suspend fun getByCategoryIds(categoryIds: List<Long>): List<WordEntity>

    /** Вставка одного слова; возвращает сгенерированный ID */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: WordEntity): Long

    /** Обновление слова */
    @Update
    suspend fun update(word: WordEntity)

    /** Удаление слова по ID */
    @Query("DELETE FROM sq_words WHERE id = :id")
    suspend fun delete(id: Long)

    /** Пакетная вставка слов встроенного контента */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>)
}
