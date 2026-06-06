package ru.internet.boardgames.counter.domain.usecase

import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import javax.inject.Inject

/** Сохранить выбранный режим отображения карточек */
class SetCardDisplayModeUseCase @Inject constructor(
    private val prefs: CounterPrefsRepository
) {
    suspend operator fun invoke(mode: CardDisplayMode) {
        prefs.setCardDisplayMode(mode)
    }
}
