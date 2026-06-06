package ru.internet.boardgames.counter.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import javax.inject.Inject

/** Получить реактивный поток режима отображения карточек */
class GetCardDisplayModeUseCase @Inject constructor(
    private val prefs: CounterPrefsRepository
) {
    operator fun invoke(): Flow<CardDisplayMode> =
        prefs.observeCardDisplayMode()
}
