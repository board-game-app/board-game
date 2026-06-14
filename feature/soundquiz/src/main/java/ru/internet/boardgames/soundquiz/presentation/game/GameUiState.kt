package ru.internet.boardgames.soundquiz.presentation.game

import ru.internet.boardgames.soundquiz.domain.model.GameSession

/**
 * UI-состояние GameScreen.
 * Является единственным источником правды для игрового экрана.
 *
 * timerRemainingMs — остаток таймера в миллисекундах (0..timerSeconds*1000).
 * timerProgress    — доля оставшегося времени (1.0..0.0) для LinearProgressIndicator.
 * passPhoneMessage — true когда таймер истёк, нужно передать телефон.
 * isGameOver       — все стопки пусты; показываем экран результатов.
 */
data class GameUiState(
    val session: GameSession? = null,
    val timerRemainingMs: Long = 0L,
    val timerProgress: Float = 1f,
    val isTimerRunning: Boolean = false,
    val passPhoneMessage: Boolean = false,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
