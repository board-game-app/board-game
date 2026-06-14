package ru.internet.boardgames.soundquiz.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.Word

interface WordRepository {

    /** Слова категории как реактивный поток — для WordManagementScreen */
    fun getWords(categoryId: Long): Flow<List<Word>>

    /** Однократное чтение слов — для GenerateGameSessionUseCase */
    suspend fun getWordsSync(categoryId: Long): List<Word>

    /**
     * Слова нескольких категорий одним запросом.
     * Оптимизация: один round-trip к БД вместо N запросов по одному.
     */
    suspend fun getWordsByCategoryIds(categoryIds: List<Long>): List<Word>

    /** Добавление слова; возвращает сгенерированный ID */
    suspend fun addWord(categoryId: Long, word: String): Long

    /** Обновление текста слова */
    suspend fun updateWord(word: Word)

    /** Удаление слова по ID */
    suspend fun deleteWord(wordId: Long)
}
