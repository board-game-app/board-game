package ru.internet.boardgames.soundquiz.domain.model

/**
 * Игровая сессия — полное состояние одной партии.
 *
 * stacks      — все стопки (включая пустые, чтобы UI мог их отображать серыми).
 * activeCard  — текущая активная карта; null означает состояние NONE (выбор стопки).
 * timerSeconds — длительность таймера для этой сессии (берётся из GameSettings).
 */
data class GameSession(
    val stacks: List<CardStack>,
    val activeCard: ActiveCard?,
    val timerSeconds: Int
) {
    /** Игра завершена когда все стопки пусты и нет активной карточки */
    val isGameOver: Boolean
        get() = activeCard == null && stacks.all { it.words.isEmpty() }

    /** Суммарное количество объяснённых слов по всем стопкам */
    val totalExplained: Int
        get() = stacks.sumOf { it.explained.size }
}
