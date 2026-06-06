package ru.internet.boardgames.spygame.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.internet.boardgames.spygame.R
import ru.internet.boardgames.spygame.domain.model.GameCard
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/**
 * Оборотная сторона карточки — показывается после переворота.
 *
 * Два режима:
 * - Обычный игрок: слово категории на [secondaryContainer]-фоне.
 * - Шпион: [stringResource(R.string.spy_word)] на [errorContainer]-фоне.
 *
 * @param card          Доменная модель карточки.
 * @param timerProgress Прогресс таймера: 1.0 (старт) → 0.0 (время вышло).
 */
@Composable
fun CardBack(
    card: GameCard,
    timerProgress: Float,
    modifier: Modifier = Modifier
) {
    val isSpy = card.isSpy

    val containerColor = if (isSpy)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val onContainerColor = if (isSpy)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    val progressColor = if (isSpy)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.secondary

    // Слово «Шпион» / «Spy» — из strings.xml, локализуется автоматически
    val displayWord = if (isSpy) stringResource(R.string.spy_word) else card.word
    val textMeasurer = rememberTextMeasurer()

    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(16.dp),
        color           = containerColor,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isSpy) {
                    Icon(
                        imageVector        = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier           = Modifier.size(64.dp),
                        tint               = onContainerColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val baseStyle = if (isSpy)
                    MaterialTheme.typography.displaySmall
                else
                    MaterialTheme.typography.headlineLarge

                // Масштабируем шрифт ДО первого рендера с помощью TextMeasurer.
                //
                // Подход «onTextLayout → обновить стейт → recompose» работает,
                // но даёт минимум 1 видимый кадр с неправильной вёрсткой —
                // особенно заметно во время анимации переворота (400 мс).
                //
                // TextMeasurer выполняет измерение синхронно внутри remember,
                // поэтому Text всегда рисуется уже с правильным размером шрифта.
                //
                // Алгоритм:
                //   1. Меряем слово при scale = 1.0.
                //   2. Если есть переполнение ИЛИ посимвольный перенос —
                //      уменьшаем scale на 10% и повторяем.
                //   3. Останавливаемся когда текст влез, или при scale = 0.4.
                //
                // Посимвольный перенос определяем через getLineStart:
                //   символ перед началом следующей строки — буква →
                //   слово разбито посимвольно, нужно уменьшить шрифт.
                //   Для «Военная база» символ перед строкой 2 — пробел →
                //   перенос по слову, масштаб не трогаем.
                BoxWithConstraints(contentAlignment = Alignment.Center) {
                    val availWidthPx = constraints.maxWidth

                    val fontScale = remember(displayWord, availWidthPx, baseStyle) {
                        var scale = 1f
                        val mc = Constraints(maxWidth = availWidthPx)
                        while (scale > 0.4f) {
                            val m = textMeasurer.measure(
                                text        = AnnotatedString(displayWord),
                                style       = baseStyle.copy(
                                    fontSize   = (baseStyle.fontSize.value   * scale).sp,
                                    lineHeight = (baseStyle.lineHeight.value * scale).sp,
                                ),
                                constraints = mc,
                                overflow    = TextOverflow.Clip,
                                maxLines    = 2,
                            )
                            val hasCharBreak = (0 until m.lineCount - 1).any { line ->
                                val ns = m.getLineStart(line + 1)
                                ns in 1..displayWord.length && displayWord[ns - 1].isLetter()
                            }
                            if (!m.hasVisualOverflow && !hasCharBreak) break
                            scale *= 0.9f
                        }
                        scale
                    }

                    Text(
                        text       = displayWord,
                        style      = baseStyle.copy(
                            fontSize   = (baseStyle.fontSize.value   * fontScale).sp,
                            lineHeight = (baseStyle.lineHeight.value * fontScale).sp,
                        ),
                        fontWeight = FontWeight.Bold,
                        color      = onContainerColor,
                        textAlign  = TextAlign.Center,
                        maxLines   = 2,
                    )
                }

                if (!isSpy) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text      = card.categoryName,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = onContainerColor.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Прогресс-бар таймера: убывает от 1.0 до 0.0 за 5 секунд
            LinearProgressIndicator(
                progress   = { timerProgress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                color      = progressColor,
                trackColor = onContainerColor.copy(alpha = 0.15f)
            )
        }
    }
}

@Preview(showBackground = true, name = "CardBack — обычный игрок")
@Composable
private fun CardBackRegularPreview() {
    SpyGameTheme {
        CardBack(
            card          = GameCard(1, false, "Пилот", "Аэропорт"),
            timerProgress = 0.6f,
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}

@Preview(showBackground = true, name = "CardBack — шпион", locale = "ru")
@Composable
private fun CardBackSpyPreview() {
    SpyGameTheme {
        CardBack(
            card          = GameCard(3, true, GameCard.SPY_WORD_PLACEHOLDER, "Аэропорт"),
            timerProgress = 0.3f,
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}
/** Длинное слово — шрифт должен уменьшиться, перенос по буквам недопустим. */
@Preview(showBackground = true, name = "CardBack — длинное слово")
@Composable
private fun CardBackLongWordPreview() {
    SpyGameTheme {
        CardBack(
            card          = GameCard(2, false, "Авиадиспетчер", "Аэропорт"),
            timerProgress = 0.8f,
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}

/** Двухсловная фраза — перенос по пробелу, масштаб не трогается. */
@Preview(showBackground = true, name = "CardBack — фраза из двух слов")
@Composable
private fun CardBackTwoWordPreview() {
    SpyGameTheme {
        CardBack(
            card          = GameCard(4, false, "Военная база", "Армия"),
            timerProgress = 1f,
            modifier      = Modifier.size(220.dp, 308.dp)
        )
    }
}
