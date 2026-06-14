package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Inject

/** Добавляет слово в категорию; возвращает ID новой записи */
class AddWordUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(categoryId: Long, word: String): Long =
        repository.addWord(categoryId, word)
}
