package ru.internet.boardgames.counter.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// ─────────────────────────────────────────────────────────────────────────────
// Palette сгенерирована через Material Theme Builder (seed: #00897B — Teal 600)
// https://m3.material.io/theme-builder
// Отличается от SpyGame (#3949AB Indigo) — разные модули, разная идентичность.
// ─────────────────────────────────────────────────────────────────────────────

// ─── Light theme ─────────────────────────────────────────────────────────────

val md_theme_light_primary            = Color(0xFF006B5E)
val md_theme_light_onPrimary          = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer   = Color(0xFF9EF2E4)
val md_theme_light_onPrimaryContainer = Color(0xFF00201B)

val md_theme_light_secondary            = Color(0xFF4A635F)
val md_theme_light_onSecondary          = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer   = Color(0xFFCCE8E3)
val md_theme_light_onSecondaryContainer = Color(0xFF051F1C)

val md_theme_light_tertiary            = Color(0xFF45616E)
val md_theme_light_onTertiary          = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer   = Color(0xFFC8E6F5)
val md_theme_light_onTertiaryContainer = Color(0xFF001E2B)

val md_theme_light_error            = Color(0xFFBA1A1A)
val md_theme_light_onError          = Color(0xFFFFFFFF)
val md_theme_light_errorContainer   = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background   = Color(0xFFFAFDFC)
val md_theme_light_onBackground = Color(0xFF191C1C)

val md_theme_light_surface          = Color(0xFFFAFDFC)
val md_theme_light_onSurface        = Color(0xFF191C1C)
val md_theme_light_surfaceVariant   = Color(0xFFDAE5E2)
val md_theme_light_onSurfaceVariant = Color(0xFF3F4947)

val md_theme_light_outline        = Color(0xFF6F7977)
val md_theme_light_outlineVariant = Color(0xFFBEC9C7)

val md_theme_light_scrim             = Color(0xFF000000)
val md_theme_light_inverseSurface    = Color(0xFF2D3130)
val md_theme_light_inverseOnSurface  = Color(0xFFEFF1F0)
val md_theme_light_inversePrimary    = Color(0xFF82D5C8)
val md_theme_light_surfaceTint       = Color(0xFF006B5E)

// ─── Dark theme ──────────────────────────────────────────────────────────────

val md_theme_dark_primary            = Color(0xFF82D5C8)
val md_theme_dark_onPrimary          = Color(0xFF003731)
val md_theme_dark_primaryContainer   = Color(0xFF005047)
val md_theme_dark_onPrimaryContainer = Color(0xFF9EF2E4)

val md_theme_dark_secondary            = Color(0xFFB1CCC7)
val md_theme_dark_onSecondary          = Color(0xFF1C3532)
val md_theme_dark_secondaryContainer   = Color(0xFF324B48)
val md_theme_dark_onSecondaryContainer = Color(0xFFCCE8E3)

val md_theme_dark_tertiary            = Color(0xFFABCBD8)
val md_theme_dark_onTertiary          = Color(0xFF163242)
val md_theme_dark_tertiaryContainer   = Color(0xFF2D4959)
val md_theme_dark_onTertiaryContainer = Color(0xFFC8E6F5)

val md_theme_dark_error            = Color(0xFFFFB4AB)
val md_theme_dark_onError          = Color(0xFF690005)
val md_theme_dark_errorContainer   = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background   = Color(0xFF191C1C)
val md_theme_dark_onBackground = Color(0xFFE0E3E2)

val md_theme_dark_surface          = Color(0xFF191C1C)
val md_theme_dark_onSurface        = Color(0xFFE0E3E2)
val md_theme_dark_surfaceVariant   = Color(0xFF3F4947)
val md_theme_dark_onSurfaceVariant = Color(0xFFBEC9C7)

val md_theme_dark_outline        = Color(0xFF899391)
val md_theme_dark_outlineVariant = Color(0xFF3F4947)

val md_theme_dark_scrim             = Color(0xFF000000)
val md_theme_dark_inverseSurface    = Color(0xFFE0E3E2)
val md_theme_dark_inverseOnSurface  = Color(0xFF2D3130)
val md_theme_dark_inversePrimary    = Color(0xFF006B5E)
val md_theme_dark_surfaceTint       = Color(0xFF82D5C8)

// ─── Seed color (для справки) ─────────────────────────────────────────────────
val seed = Color(0xFF00897B)

// ─── Палитра цветов счётчиков (§7.3 ТЗ) ──────────────────────────────────────
// Используется в ColorPicker при создании и редактировании счётчика.
// Порядок строго соответствует ТЗ — важен для отображения в UI.

val CounterColorOrange     = Color(0xFFF5A623)
val CounterColorRed        = Color(0xFFE53935)
val CounterColorPink       = Color(0xFFE91E8C)
val CounterColorVioletPink = Color(0xFF9C27B0)
val CounterColorViolet     = Color(0xFF7B1FA2)
val CounterColorNavy       = Color(0xFF3949AB)
val CounterColorBlue       = Color(0xFF1E88E5)

/**
 * Упорядоченная палитра счётчиков (§7.3).
 * Индекс в списке = порядок отображения кружков в ColorPicker.
 * Хранится как [Color]; для БД используйте [Color.value] → Long.
 */
val CounterColorPalette: List<Color> = listOf(
    CounterColorOrange,
    CounterColorRed,
    CounterColorPink,
    CounterColorVioletPink,
    CounterColorViolet,
    CounterColorNavy,
    CounterColorBlue
)

/**
 * Цвет по умолчанию для нового счётчика (первый в палитре — оранжевый).
 */
val CounterColorDefault = CounterColorOrange

/**
 * Тёмный фон верхней части компактной карточки счётчика (§5.1).
 * Намеренно не привязан к Material-теме: карточка должна выглядеть одинаково
 * в светлой и тёмной системной теме.
 */
val CounterCardTopBackground = Color(0xFF1A1C2E)

/** ARGB Long-значения палитры для хранения в Room (§7.3) */
val CounterColorPaletteArgb: List<Long> = listOf(
    0xFFF5A623L, 0xFFE53935L, 0xFFE91E8CL,
    0xFF9C27B0L, 0xFF7B1FA2L, 0xFF3949ABL, 0xFF1E88E5L
)

/** Long ARGB → Compose Color для отображения карточки */
fun Long.toCounterColor(): androidx.compose.ui.graphics.Color =
    androidx.compose.ui.graphics.Color(this.toInt())

/** Compose Color → Long ARGB для сохранения в Room */
fun androidx.compose.ui.graphics.Color.toCounterColorArgb(): Long =
    this.toArgb().toLong() and 0xFFFFFFFFL