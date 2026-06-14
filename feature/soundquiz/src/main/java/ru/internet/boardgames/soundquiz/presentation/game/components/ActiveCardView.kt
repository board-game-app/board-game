package ru.internet.boardgames.soundquiz.presentation.game.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.soundquiz.R
import ru.internet.boardgames.soundquiz.domain.model.ActiveCard
import ru.internet.boardgames.soundquiz.domain.model.CardState
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp

@Composable
fun ActiveCardView(
    activeCard: ActiveCard,
    categoryName: String,
    cardColor: Int,
    timerProgress: Float,
    timerRemainingMs: Long,
    onCardTap: () -> Unit,
    onWordExplained: () -> Unit,
    onFirstWordPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRevealed = activeCard.state == CardState.REVEALED

    val rotationY by animateFloatAsState(
        targetValue    = if (isRevealed) 180f else 0f,
        animationSpec  = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label          = "card_flip_rotation"
    )
    val showFront = rotationY > 90f

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ElevatedCard(
            onClick   = onCardTap,
            enabled   = activeCard.state == CardState.FACE_DOWN,
            modifier  = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer {
                    this.rotationY    = rotationY
                    cameraDistance    = 12f * density
                },
            shape     = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp)
        ) {
            if (showFront) {
                FrontFace(
                    word             = activeCard.word.word,
                    timerProgress    = timerProgress,
                    timerRemainingMs = timerRemainingMs,
                    onWordExplained  = onWordExplained,
                    onFirstWordPress = onFirstWordPress,
                    modifier         = Modifier.graphicsLayer { this.rotationY = 180f }
                )
            } else {
                BackFace(categoryName = categoryName, cardColor = Color(cardColor))
            }
        }
    }
}

@Composable
private fun BackFace(categoryName: String, cardColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier               = modifier.fillMaxWidth().height(280.dp).padding(24.dp),
        horizontalAlignment    = Alignment.CenterHorizontally,
        verticalArrangement    = Arrangement.Center
    ) {
        Text("♪", style = MaterialTheme.typography.displayMedium,
            color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(categoryName, style = MaterialTheme.typography.headlineSmall,
            color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.sound_quiz_tap_to_flip),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center)
    }
}

/**
 * Лицо карточки: слово с блюром + таймер + кнопка «Объяснено».
 *
 * Механика «пик-и-скрой»:
 * Слово по умолчанию размыто (16dp blur).
 * Пока пользователь удерживает палец на карточке — слово чёткое.
 * При отпускании — снова размывается.
 * Это скрывает слово от посторонних, пока объясняющий не готов его видеть.
 */
@Composable
private fun FrontFace(
    word: String,
    timerProgress: Float,
    timerRemainingMs: Long,
    onWordExplained: () -> Unit,
    onFirstWordPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isWordVisible by remember { mutableStateOf(false) }

    /**
     * Флаг «таймер уже запущен для этого показа карточки».
     * remember без ключа: сбрасывается в false каждый раз, когда FrontFace
     * уходит из композиции (карта перевернулась рубашкой) и возвращается снова.
     * Это гарантирует, что при следующей попытке объяснить то же слово
     * таймер снова будет ждать первого касания.
     */
    var hasTimerStarted by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue   = if (isWordVisible) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 150),
        label         = "word_blur"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(24.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event      = awaitPointerEvent(PointerEventPass.Initial)
                        val anyPressed = event.changes.any { it.pressed }

                        // Первое касание: снимаем блюр и одновременно стартуем таймер
                        if (anyPressed && !hasTimerStarted) {
                            hasTimerStarted = true
                            onFirstWordPress()
                        }
                        if (isWordVisible != anyPressed) {
                            isWordVisible = anyPressed
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TimerIndicator(
            timerRemainingMs = timerRemainingMs,
            timerProgress    = timerProgress,
            modifier         = Modifier.fillMaxWidth()
        )

        // Слово с blur-эффектом: читаемо только при нажатии
        Text(
            text      = word,
            style     = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .fillMaxWidth()
                .blur(blurRadius)
        )

        Button(onClick = onWordExplained, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.sound_quiz_explained),
                style = MaterialTheme.typography.titleMedium)
        }
    }
}
