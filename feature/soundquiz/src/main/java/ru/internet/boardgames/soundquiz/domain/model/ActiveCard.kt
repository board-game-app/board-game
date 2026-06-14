package ru.internet.boardgames.soundquiz.domain.model

/**
 * Состояния активной карточки.
 *
 * FACE_DOWN      — карточка «рубашкой» вверх (слово скрыто, таймер не идёт).
 * REVEALED       — карточка перевёрнута, слово видно, таймер запущен.
 * ANIMATING_BACK — обратный flip-анимация после истечения таймера.
 *
 * NONE (нет активной карты) представлено как activeCard == null в GameSession.
 * DONE (карта объяснена) — карта удаляется из стека, activeCard становится null.
 */
enum class CardState {
    FACE_DOWN,
    REVEALED,
    ANIMATING_BACK
}

/**
 * Активная карточка — единственная карта в игре, с которой в данный момент работает игрок.
 * Остаётся активной до успеха (DONE) или до тех пор пока следующий игрок не перевернёт её снова.
 */
data class ActiveCard(
    /** Индекс стопки в GameSession.stacks, из которой взята карточка */
    val stackIndex: Int,
    val word: Word,
    val state: CardState
)
