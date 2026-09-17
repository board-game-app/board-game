package ru.internet.boardgames.soundquiz.presentation.game.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.soundquiz.R
import androidx.compose.ui.tooling.preview.Preview
import ru.internet.boardgames.soundquiz.domain.model.ActiveCard
import ru.internet.boardgames.soundquiz.domain.model.CardState
import ru.internet.boardgames.soundquiz.domain.model.Word
import androidx.compose.ui.input.pointer.PointerEventPass

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
        targetValue   = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label         = "card_flip_rotation"
    )
    val showFront = rotationY > 90f

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ElevatedCard(
            onClick   = onCardTap,
            enabled   = activeCard.state == CardState.FACE_DOWN,
            modifier  = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer {
                    this.rotationY = rotationY
                    cameraDistance = 12f * density
                },
            shape     = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 16.dp),
            colors    = CardDefaults.elevatedCardColors(
                containerColor = Color(cardColor)
            )
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
                BackFace(categoryName = categoryName)
            }
        }
    }
}

/**
 * Рубашка карточки.
 *
 * Фон задаётся на уровне [ElevatedCard] через [CardDefaults.elevatedCardColors],
 * поэтому [cardColor] здесь больше не нужен — параметр убран во избежание путаницы.
 * Весь текст отображается через [OutlinedText]: тонкая тёмная обводка обеспечивает
 * читаемость на любом цвете категории без перебора с контрастом.
 */
@Composable
private fun BackFace(categoryName: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка — без изменений
        OutlinedText(
            text         = "♪",
            style        = MaterialTheme.typography.displayMedium,
            color        = Color.White.copy(alpha = 1f),
            textAlign    = TextAlign.Center,
            outlineWidth = 6f
        )
        Spacer(Modifier.height(16.dp))
        // Название категории — главный элемент (как слово в CardBack шпиона):
        OutlinedText(
            text         = categoryName,
            style        = MaterialTheme.typography.headlineLarge.copy(
                               fontWeight = FontWeight.Bold
                           ),
            color        = Color.White,
            textAlign    = TextAlign.Center,
            outlineWidth = 7f
        )
        Spacer(Modifier.height(12.dp))
        // Подсказка — второстепенный элемент (как подпись категории в CardBack шпиона):
        OutlinedText(
            text         = stringResource(R.string.sound_quiz_tap_to_flip),
            style        = MaterialTheme.typography.bodyMedium,
            color        = Color.White.copy(alpha = 0.90f),
            outlineColor = Color.Black.copy(alpha = 0.20f),
            outlineWidth = 4f,
            textAlign    = TextAlign.Center
        )
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

        Text(
            text      = word,
            style     = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .fillMaxWidth()
                .blur(blurRadius)
        )

        Button(onClick = onWordExplained, modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = stringResource(R.string.sound_quiz_explained),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewWord = Word(id = 1L, categoryId = 1L, word = "Контрабас")
private const val PREVIEW_COLOR = 0xFF2196F3.toInt() // Material Blue 500

/** Карточка рубашкой вверх — игрок ещё не перевернул её */
@Preview(name = "ActiveCard — рубашка (FACE_DOWN)", showBackground = true)
@Composable
private fun PreviewActiveCard_FaceDown() {
    MaterialTheme {
        ActiveCardView(
            activeCard       = ActiveCard(stackIndex = 0, word = previewWord, state = CardState.FACE_DOWN),
            categoryName     = "Музыка",
            cardColor        = PREVIEW_COLOR,
            timerProgress    = 1f,
            timerRemainingMs = 60_000L,
            onCardTap        = {},
            onWordExplained  = {},
            onFirstWordPress = {}
        )
    }
}

/** Карточка лицом — таймер идёт, слово отображается (в превью — с blur, т.к. палец не нажат) */
@Preview(name = "ActiveCard — лицо, таймер ~50% (REVEALED)", showBackground = true)
@Composable
private fun PreviewActiveCard_Revealed() {
    MaterialTheme {
        ActiveCardView(
            activeCard       = ActiveCard(stackIndex = 0, word = previewWord, state = CardState.REVEALED),
            categoryName     = "Музыка",
            cardColor        = PREVIEW_COLOR,
            timerProgress    = 0.5f,
            timerRemainingMs = 30_000L,
            onCardTap        = {},
            onWordExplained  = {},
            onFirstWordPress = {}
        )
    }
}

/** Карточка лицом — таймер критически мал */
@Preview(name = "ActiveCard — лицо, таймер < 10% (REVEALED)", showBackground = true)
@Composable
private fun PreviewActiveCard_RevealedLowTimer() {
    MaterialTheme {
        ActiveCardView(
            activeCard       = ActiveCard(stackIndex = 0, word = previewWord, state = CardState.REVEALED),
            categoryName     = "Музыка",
            cardColor        = PREVIEW_COLOR,
            timerProgress    = 0.08f,
            timerRemainingMs = 4_800L,
            onCardTap        = {},
            onWordExplained  = {},
            onFirstWordPress = {}
        )
    }
}
