package ru.internet.boardgames.spygame.presentation.game

import ru.internet.boardgames.spygame.domain.model.GameSession

// ─────────────────────────────────────────
// Состояние отдельной карточки
// ─────────────────────────────────────────

/**
 * Жизненный цикл карточки в UI.
 *
 * ```
 * STACKED ──(tap)──► REVEALED ──(tap / swipe / timer)──► DISMISSED
 * ```
 */
enum class CardUiState {
    /** Карточка в стопке, рубашкой вверх. Только верхняя реагирует на тап. */
    STACKED,

    /** Карточка перевёрнута, слово видно. Таймер идёт. */
    REVEALED,

    /** Карточка отброшена (анимация slide-out завершена или запущена). */
    DISMISSED
}

// ─────────────────────────────────────────
// Состояние экрана
// ─────────────────────────────────────────

/**
 * Полное состояние игрового экрана.
 *
 * @param session       Текущая игровая сессия. null — ещё не загружена.
 * @param cardStates    Список состояний карточек. Позиция = индекс в [GameSession.cards].
 * @param timerProgress Прогресс таймера: 1.0 → 0.0 за [GameViewModel.TIMER_DURATION_MS].
 *                      Используется в [LinearProgressIndicator] на CardBack.
 * @param isLoading     true пока идёт загрузка новой сессии.
 * @param error         Сообщение ошибки (БД пуста, сеть недоступна и т.п.).
 */
data class GameUiState(
    val session: GameSession? = null,
    val cardStates: List<CardUiState> = emptyList(),
    val timerProgress: Float = 1f,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /**
     * Позиция верхней карточки стопки — наименьший индекс среди [CardUiState.STACKED].
     * null если все карточки открыты или отброшены.
     *
     * Спецификация: «определять как карточку с наименьшим индексом среди STACKED».
     */
    val topStackedPosition: Int?
        get() = cardStates.indexOfFirst { it == CardUiState.STACKED }.takeIf { it >= 0 }

    /**
     * Позиция открытой карточки ([CardUiState.REVEALED]).
     * null если нет открытых. В один момент может быть открыта только одна.
     */
    val revealedPosition: Int?
        get() = cardStates.indexOfFirst { it == CardUiState.REVEALED }.takeIf { it >= 0 }

    /**
     * true если все карточки отброшены — все игроки посмотрели свои роли.
     * Используется для показа финального состояния / кнопки «Новая игра».
     */
    val isGameComplete: Boolean
        get() = session != null &&
                cardStates.isNotEmpty() &&
                cardStates.all { it == CardUiState.DISMISSED }
}
