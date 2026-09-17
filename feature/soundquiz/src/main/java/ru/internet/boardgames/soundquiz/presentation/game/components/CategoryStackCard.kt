package ru.internet.boardgames.soundquiz.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import ru.internet.boardgames.soundquiz.domain.model.CardStack
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.domain.model.Word

/**
 * Карточка стопки категорий на игровом поле.
 *
 * Рубашка стопки: цветной фон (colorArgb), название категории,
 * счётчик оставшихся карт.
 * Пустая стопка (words.isEmpty): серый цвет, недоступна для нажатия.
 *
 * Цвет текста вычисляется через [adaptiveTextColor] — тёмный на светлых
 * фонах (жёлтый, светло-зелёный), светлый на тёмных. [OutlinedText] не нужен.
 * Мягкая тень ([softShadow]) вместо двойного Stroke обеспечивает читаемость
 * без артефактов по краям букв.
 */
@Composable
fun CategoryStackCard(
    stack: CardStack,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmpty = stack.words.isEmpty()
    val cardColor = if (isEmpty) MaterialTheme.colorScheme.surfaceVariant
                    else Color(stack.colorArgb)
    val textColor = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant
                    else adaptiveTextColor(stack.colorArgb)

    ElevatedCard(
        onClick = onClick,
        enabled = isEnabled && !isEmpty,
        modifier = modifier.aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor,
            disabledContainerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isEmpty) {
                Text(
                    text      = "♪",
                    style     = MaterialTheme.typography.displaySmall.copy(
                                    color  = textColor,
                                    shadow = softShadow(textColor)
                                ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }

            AutoShrinkText(
                text    = stack.category.name,
                style   = MaterialTheme.typography.headlineLarge.copy(
                              fontWeight = FontWeight.Bold
                          ),
                color   = textColor,
                shadow  = if (isEmpty) null else softShadow(textColor),
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            WordCountPill(count = stack.words.size, textColor = textColor)
        }
    }
}

// ─── Вспомогательные компоненты ──────────────────────────────────────────────

/**
 * Пилюля со счётчиком слов.
 *
 * Фон — тот же цвет, что у текста, с низкой непрозрачностью.
 * Это позволяет не хардкодить цвет и сохранять гармонию
 * как на тёмных, так и на светлых карточках.
 */
@Composable
private fun WordCountPill(count: Int, textColor: Color) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(50))
            .background(textColor.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = textColor
                    )
        )
    }
}

/**
 * Text с автоматическим уменьшением шрифта.
 *
 * При переполнении контейнера (hasVisualOverflow) fontSize сжимается
 * на 15% за каждую итерацию до минимума [minFontSizeSp].
 * Текст не отображается, пока измерение не завершено — без мерцания.
 */
@Composable
private fun AutoShrinkText(
    text:          String,
    style:         TextStyle,
    color:         Color,
    maxLines:      Int,
    modifier:      Modifier = Modifier,
    shadow:        Shadow?  = null,
    minFontSizeSp: Float    = 10f
) {
    var currentStyle by remember(text, style, color, shadow) {
        mutableStateOf(style.copy(color = color, shadow = shadow))
    }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text      = text,
        style     = currentStyle,
        maxLines  = maxLines,
        overflow  = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        onTextLayout = { result ->
            if (result.hasVisualOverflow) {
                val reduced = (currentStyle.fontSize.value * 0.85f).coerceAtLeast(minFontSizeSp)
                currentStyle = currentStyle.copy(fontSize = reduced.sp)
            } else {
                readyToDraw = true
            }
        },
        modifier  = modifier.drawWithContent { if (readyToDraw) drawContent() }
    )
}

// ─── Утилиты ─────────────────────────────────────────────────────────────────

/**
 * Мягкая тень для текста на цветном фоне.
 *
 * Тёмная тень для светлого текста (белый на синем),
 * светлая тень для тёмного текста (чёрный на жёлтом).
 */
private fun softShadow(textColor: Color): Shadow {
    val textLum = textColor.red * 0.2126f + textColor.green * 0.7152f + textColor.blue * 0.0722f
    val shadowColor = if (textLum > 0.5f) Color.Black.copy(alpha = 0.45f)
                      else Color.White.copy(alpha = 0.40f)
    return Shadow(color = shadowColor, offset = Offset(0f, 1f), blurRadius = 4f)
}

/**
 * Адаптивный цвет текста на основе относительной яркости по WCAG.
 *
 * Возвращает тёмный (#212121) текст для светлых фонов и белый — для тёмных.
 * Порог 0.35 обеспечивает соотношение контраста ≥ 4.5:1 (WCAG AA).
 */
private fun adaptiveTextColor(argb: Int): Color {
    val c = Color(argb)
    fun lin(v: Float): Float =
        if (v <= 0.03928f) v / 12.92f
        else ((v + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    val luminance = 0.2126f * lin(c.red) + 0.7152f * lin(c.green) + 0.0722f * lin(c.blue)
    return if (luminance > 0.35f) Color(0xFF212121) else Color.White
}

// ─── Previews ────────────────────────────────────────────────────────────────

private fun fakeStack(
    name:          String,
    colorArgb:     Int,
    wordCount:     Int,
    explainedCount: Int = 0
): CardStack {
    val category = Category(
        id = 1L, name = name,
        colorArgb    = colorArgb,
        isBuiltIn    = true,
        wordCount    = wordCount + explainedCount,
        language     = "ru"
    )
    return CardStack(
        category  = category,
        colorArgb = colorArgb,
        words     = List(wordCount)       { Word(it.toLong(), 1L, "Слово") },
        explained = List(explainedCount)  { Word((100 + it).toLong(), 1L, "Слово") }
    )
}

/** Обычная карточка — тёмный фон, светлый текст */
@Preview(name = "Стопка — тёмный фон (Музыка)", showBackground = true, widthDp = 160)
@Composable
private fun PreviewCategoryStackCard_DarkBg() {
    MaterialTheme {
        CategoryStackCard(
            stack     = fakeStack("Музыка", 0xFF1565C0.toInt(), wordCount = 7),
            isEnabled = true,
            onClick   = {}
        )
    }
}

/** Светлый фон — проверяем адаптивный тёмный текст (был: белый на жёлтом) */
@Preview(name = "Стопка — светлый фон (История)", showBackground = true, widthDp = 160)
@Composable
private fun PreviewCategoryStackCard_LightBg() {
    MaterialTheme {
        CategoryStackCard(
            stack     = fakeStack("История", 0xFFF9A825.toInt(), wordCount = 5),
            isEnabled = true,
            onClick   = {}
        )
    }
}

/** Длинное название — проверяем AutoShrinkText (был: перенос по буквам) */
@Preview(name = "Стопка — длинное название", showBackground = true, widthDp = 160)
@Composable
private fun PreviewCategoryStackCard_LongName() {
    MaterialTheme {
        CategoryStackCard(
            stack     = fakeStack("Знаменитые личности", 0xFFBF360C.toInt(), wordCount = 3),
            isEnabled = true,
            onClick   = {}
        )
    }
}

/** Пустая стопка — серый фон, кнопка задизейблена */
@Preview(name = "Стопка — пустая", showBackground = true, widthDp = 160)
@Composable
private fun PreviewCategoryStackCard_Empty() {
    MaterialTheme {
        CategoryStackCard(
            stack     = fakeStack("Кино", 0xFF388E3C.toInt(), wordCount = 0, explainedCount = 5),
            isEnabled = true,
            onClick   = {}
        )
    }
}
