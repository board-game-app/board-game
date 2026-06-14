package ru.internet.boardgames.soundquiz.domain.usecase

import ru.internet.boardgames.soundquiz.domain.model.Word
import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Inject

/** Обновляет текст слова */
class UpdateWordUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(word: Word) =
        repository.updateWord(word)
}
