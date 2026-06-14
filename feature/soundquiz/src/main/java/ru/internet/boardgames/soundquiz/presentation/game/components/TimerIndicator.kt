package ru.internet.boardgames.soundquiz.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.soundquiz.R

/**
 * Индикатор таймера: LinearProgressIndicator + цифровой счётчик секунд.
 * При остатке < 10 секунд прогресс-бар и текст переключаются на цвет error
 * для визуального предупреждения игрока.
 */
@Composable
fun TimerIndicator(
    timerRemainingMs: Long,
    timerProgress: Float,
    modifier: Modifier = Modifier
) {
    val isLow = timerRemainingMs in 1L..9_999L
    val indicatorColor = if (isLow) MaterialTheme.colorScheme.error
                         else MaterialTheme.colorScheme.primary
    val trackColor = if (isLow) MaterialTheme.colorScheme.errorContainer
                     else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isLow) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface

    // Округление вверх: 4500мс → «5 с», 1мс → «1 с», 0мс → «0 с»
    val secondsRemaining = ((timerRemainingMs + 999L) / 1000L).coerceAtLeast(0L)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.sound_quiz_settings_timer_value, secondsRemaining),
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
        LinearProgressIndicator(
            progress = { timerProgress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = indicatorColor,
            trackColor = trackColor
        )
    }
}
