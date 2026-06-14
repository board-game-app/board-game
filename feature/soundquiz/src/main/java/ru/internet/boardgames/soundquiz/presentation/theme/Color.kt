package ru.internet.boardgames.soundquiz.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Цвета Material3 для seed #00897B (Teal / Бирюзовый)
// Сгенерированы через Material Theme Builder: https://m3.material.io/theme-builder
// ─────────────────────────────────────────────────────────────────────────────

// Светлая тема
private val light_primary             = Color(0xFF006A60)
private val light_onPrimary           = Color(0xFFFFFFFF)
private val light_primaryContainer    = Color(0xFF74F8E5)
private val light_onPrimaryContainer  = Color(0xFF00201C)
private val light_secondary           = Color(0xFF4A6360)
private val light_onSecondary         = Color(0xFFFFFFFF)
private val light_secondaryContainer  = Color(0xFFCCE8E4)
private val light_onSecondaryContainer = Color(0xFF051F1D)
private val light_tertiary            = Color(0xFF45626E)
private val light_onTertiary          = Color(0xFFFFFFFF)
private val light_tertiaryContainer   = Color(0xFFC8E6F5)
private val light_onTertiaryContainer = Color(0xFF001F29)
private val light_error               = Color(0xFFBA1A1A)
private val light_onError             = Color(0xFFFFFFFF)
private val light_errorContainer      = Color(0xFFFFDAD6)
private val light_onErrorContainer    = Color(0xFF410002)
private val light_background          = Color(0xFFFAFDFC)
private val light_onBackground        = Color(0xFF191C1B)
private val light_surface             = Color(0xFFFAFDFC)
private val light_onSurface           = Color(0xFF191C1B)
private val light_surfaceVariant      = Color(0xFFDAE5E2)
private val light_onSurfaceVariant    = Color(0xFF3F4947)
private val light_outline             = Color(0xFF6F7977)
private val light_outlineVariant      = Color(0xFFBEC9C6)
private val light_scrim               = Color(0xFF000000)
private val light_inverseSurface      = Color(0xFF2D3130)
private val light_inverseOnSurface    = Color(0xFFEFF1EF)
private val light_inversePrimary      = Color(0xFF52DBC8)
private val light_surfaceTint         = Color(0xFF006A60)

// Тёмная тема
private val dark_primary              = Color(0xFF52DBC8)
private val dark_onPrimary            = Color(0xFF003731)
private val dark_primaryContainer     = Color(0xFF005048)
private val dark_onPrimaryContainer   = Color(0xFF74F8E5)
private val dark_secondary            = Color(0xFFB0CCCA)
private val dark_onSecondary          = Color(0xFF1B3533)
private val dark_secondaryContainer   = Color(0xFF324B49)
private val dark_onSecondaryContainer = Color(0xFFCCE8E4)
private val dark_tertiary             = Color(0xFFADCAE0)
private val dark_onTertiary           = Color(0xFF143244)
private val dark_tertiaryContainer    = Color(0xFF2D4A5B)
private val dark_onTertiaryContainer  = Color(0xFFC8E6F5)
private val dark_error                = Color(0xFFFFB4AB)
private val dark_onError              = Color(0xFF690005)
private val dark_errorContainer       = Color(0xFF93000A)
private val dark_onErrorContainer     = Color(0xFFFFDAD6)
private val dark_background           = Color(0xFF191C1B)
private val dark_onBackground         = Color(0xFFE1E3E1)
private val dark_surface              = Color(0xFF191C1B)
private val dark_onSurface            = Color(0xFFE1E3E1)
private val dark_surfaceVariant       = Color(0xFF3F4947)
private val dark_onSurfaceVariant     = Color(0xFFBEC9C6)
private val dark_outline              = Color(0xFF899390)
private val dark_outlineVariant       = Color(0xFF3F4947)
private val dark_scrim                = Color(0xFF000000)
private val dark_inverseSurface       = Color(0xFFE1E3E1)
private val dark_inverseOnSurface     = Color(0xFF2D3130)
private val dark_inversePrimary       = Color(0xFF006A60)
private val dark_surfaceTint          = Color(0xFF52DBC8)

// ─────────────────────────────────────────────────────────────────────────────
// Готовые схемы для использования в SoundQuizTheme
// ─────────────────────────────────────────────────────────────────────────────

internal val SoundQuizLightColorScheme = lightColorScheme(
    primary             = light_primary,
    onPrimary           = light_onPrimary,
    primaryContainer    = light_primaryContainer,
    onPrimaryContainer  = light_onPrimaryContainer,
    secondary           = light_secondary,
    onSecondary         = light_onSecondary,
    secondaryContainer  = light_secondaryContainer,
    onSecondaryContainer = light_onSecondaryContainer,
    tertiary            = light_tertiary,
    onTertiary          = light_onTertiary,
    tertiaryContainer   = light_tertiaryContainer,
    onTertiaryContainer = light_onTertiaryContainer,
    error               = light_error,
    onError             = light_onError,
    errorContainer      = light_errorContainer,
    onErrorContainer    = light_onErrorContainer,
    background          = light_background,
    onBackground        = light_onBackground,
    surface             = light_surface,
    onSurface           = light_onSurface,
    surfaceVariant      = light_surfaceVariant,
    onSurfaceVariant    = light_onSurfaceVariant,
    outline             = light_outline,
    outlineVariant      = light_outlineVariant,
    scrim               = light_scrim,
    inverseSurface      = light_inverseSurface,
    inverseOnSurface    = light_inverseOnSurface,
    inversePrimary      = light_inversePrimary,
    surfaceTint         = light_surfaceTint,
)

internal val SoundQuizDarkColorScheme = darkColorScheme(
    primary             = dark_primary,
    onPrimary           = dark_onPrimary,
    primaryContainer    = dark_primaryContainer,
    onPrimaryContainer  = dark_onPrimaryContainer,
    secondary           = dark_secondary,
    onSecondary         = dark_onSecondary,
    secondaryContainer  = dark_secondaryContainer,
    onSecondaryContainer = dark_onSecondaryContainer,
    tertiary            = dark_tertiary,
    onTertiary          = dark_onTertiary,
    tertiaryContainer   = dark_tertiaryContainer,
    onTertiaryContainer = dark_onTertiaryContainer,
    error               = dark_error,
    onError             = dark_onError,
    errorContainer      = dark_errorContainer,
    onErrorContainer    = dark_onErrorContainer,
    background          = dark_background,
    onBackground        = dark_onBackground,
    surface             = dark_surface,
    onSurface           = dark_onSurface,
    surfaceVariant      = dark_surfaceVariant,
    onSurfaceVariant    = dark_onSurfaceVariant,
    outline             = dark_outline,
    outlineVariant      = dark_outlineVariant,
    scrim               = dark_scrim,
    inverseSurface      = dark_inverseSurface,
    inverseOnSurface    = dark_inverseOnSurface,
    inversePrimary      = dark_inversePrimary,
    surfaceTint         = dark_surfaceTint,
)

// ─────────────────────────────────────────────────────────────────────────────
// Предопределённые цвета стопок (8 вариантов для CategoryEntity.colorArgb).
// Используются в ColorPicker на экране создания/редактирования категории.
// Значения совпадают с ARGB Int в JSON-файлах assets.
// ─────────────────────────────────────────────────────────────────────────────

val CardPaletteColors = listOf(
    Color(0xFFE53935), // Красный
    Color(0xFFFB8C00), // Оранжевый
    Color(0xFFFDD835), // Жёлтый
    Color(0xFF43A047), // Зелёный
    Color(0xFF039BE5), // Голубой
    Color(0xFF1E88E5), // Синий
    Color(0xFF8E24AA), // Фиолетовый
    Color(0xFFD81B60), // Розовый
)
