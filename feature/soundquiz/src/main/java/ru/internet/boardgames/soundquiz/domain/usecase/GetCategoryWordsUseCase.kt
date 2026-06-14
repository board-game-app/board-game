package ru.internet.boardgames.soundquiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.soundquiz.domain.model.Word
import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Inject

/** Возвращает слова категории как реактивный поток */
class GetCategoryWordsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(categoryId: Long): Flow<List<Word>> =
        repository.getWords(categoryId)
}
