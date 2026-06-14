package ru.internet.boardgames.soundquiz.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.soundquiz.data.local.db.WordDao
import ru.internet.boardgames.soundquiz.data.local.db.WordEntity
import ru.internet.boardgames.soundquiz.domain.model.Word
import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    /** Слова категории как реактивный поток — для WordManagementScreen */
    override fun getWords(categoryId: Long): Flow<List<Word>> =
        wordDao.getByCategory(categoryId).map { list ->
            list.map { it.toDomain() }
        }

    /** Однократное чтение слов — для GenerateGameSessionUseCase */
    override suspend fun getWordsSync(categoryId: Long): List<Word> =
        wordDao.getByCategorySync(categoryId).map { it.toDomain() }

    /** Слова нескольких категорий сразу — оптимизация для генерации сессии */
    override suspend fun getWordsByCategoryIds(categoryIds: List<Long>): List<Word> =
        wordDao.getByCategoryIds(categoryIds).map { it.toDomain() }

    /**
     * Добавление пользовательского слова.
     * Язык слова наследуется от категории — не передаётся явно, чтобы
     * не усложнять интерфейс use case.
     */
    override suspend fun addWord(categoryId: Long, word: String): Long {
        val entity = WordEntity(
            id = 0L,
            categoryId = categoryId,
            word = word,
            language = ""
        )
        return wordDao.insert(entity)
    }

    /** Обновление текста слова */
    override suspend fun updateWord(word: Word) {
        wordDao.update(
            WordEntity(
                id = word.id,
                categoryId = word.categoryId,
                word = word.word,
                language = ""
            )
        )
    }

    /** Удаление слова по ID */
    override suspend fun deleteWord(wordId: Long) {
        wordDao.delete(wordId)
    }
}

/** Маппинг WordEntity → доменная модель Word */
private fun WordEntity.toDomain() = Word(
    id = id,
    categoryId = categoryId,
    word = word
)
