package ru.internet.boardgames.soundquiz.domain.model

/**
 * Стопка карточек одной категории.
 *
 * words     — слова, которые ещё не объяснены в текущей сессии (уменьшается).
 * explained — слова, успешно объяснённые в текущей сессии (растёт).
 *
 * Стопка считается пустой когда words.isEmpty().
 * Активная карта хранится отдельно в GameSession.activeCard, а не в стопке.
 */
data class CardStack(
    val category: Category,
    val colorArgb: Int,
    val words: List<Word>,
    val explained: List<Word>
)
