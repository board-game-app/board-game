package ru.internet.boardgames.spygame.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordDao {

    /**
     * Возвращает все слова для заданной категории и языка.
     * Вызывается при построении доменной модели [Category].
     */
    @Query(
        """
        SELECT * FROM words 
        WHERE categoryId = :categoryId 
          AND language   = :language
        """
    )
    suspend fun getWordsByCategoryAndLanguage(
        categoryId: String,
        language: String
    ): List<WordEntity>

    /**
     * Пакетная вставка слов. REPLACE — безопасно при повторном seeding-е.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    /**
     * Полная очистка таблицы слов.
     * Вызывается явно перед вставкой нового контента,
     * хотя ForeignKey CASCADE уже гарантирует удаление
     * при очистке categories.
     */
    @Query("DELETE FROM words")
    suspend fun deleteAll()
}
