package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Inject

/** Удаляет слово по ID */
class DeleteWordUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long) =
        repository.deleteWord(wordId)
}
