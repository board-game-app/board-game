package ru.internet.boardgames.counter.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографическая шкала модуля :feature:counter.
 *
 * Ключевые размеры для карточек счётчика:
 *   displayMedium (48sp, Bold) → значение в компактной карточке (§5.1 ~48sp)
 *   displaySmall  (40sp, Bold) → значение в широкой карточке    (§5.2 ~40sp)
 *   titleSmall    (14sp)       → название счётчика над карточкой (§5.1, §5.2)
 *   labelLarge    (14sp)       → текст кнопок быстрых действий   (§5.3)
 */
val CounterTypography = Typography(

    // ─── Display — крупные числа значений счётчиков ───────────────────────────
    displayLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Bold,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // Компактная карточка: значение ~48sp (§5.1)
    displayMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Bold,
        fontSize      = 48.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp
    ),
    // Широкая карточка: значение ~40sp (§5.2)
    displaySmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp
    ),

    // ─── Headline ─────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),

    // ─── Title — AppBar, заголовки экранов и диалогов ─────────────────────────
    titleLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Название счётчика над карточкой (§5.1, §5.2 ~14sp)
    titleSmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ─── Body ─────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ─── Label — кнопки быстрых действий, ярлыки полей ввода (§5.3, §7) ──────
    labelLarge = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = FontFamily.SansSerif,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp
    )
)
