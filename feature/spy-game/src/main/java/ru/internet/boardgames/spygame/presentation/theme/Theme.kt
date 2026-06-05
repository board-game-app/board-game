package ru.internet.boardgames.spygame.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ─── Статические цветовые схемы (используются на Android < 12) ───────────────

private val LightColorScheme = lightColorScheme(
    primary            = md_theme_light_primary,
    onPrimary          = md_theme_light_onPrimary,
    primaryContainer   = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,

    secondary            = md_theme_light_secondary,
    onSecondary          = md_theme_light_onSecondary,
    secondaryContainer   = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,

    tertiary            = md_theme_light_tertiary,
    onTertiary          = md_theme_light_onTertiary,
    tertiaryContainer   = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,

    error            = md_theme_light_error,
    onError          = md_theme_light_onError,
    errorContainer   = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,

    background   = md_theme_light_background,
    onBackground = md_theme_light_onBackground,

    surface          = md_theme_light_surface,
    onSurface        = md_theme_light_onSurface,
    surfaceVariant   = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,

    outline        = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,

    scrim            = md_theme_light_scrim,
    inverseSurface   = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary   = md_theme_light_inversePrimary,
    surfaceTint      = md_theme_light_surfaceTint
)

private val DarkColorScheme = darkColorScheme(
    primary            = md_theme_dark_primary,
    onPrimary          = md_theme_dark_onPrimary,
    primaryContainer   = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,

    secondary            = md_theme_dark_secondary,
    onSecondary          = md_theme_dark_onSecondary,
    secondaryContainer   = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,

    tertiary            = md_theme_dark_tertiary,
    onTertiary          = md_theme_dark_onTertiary,
    tertiaryContainer   = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,

    error            = md_theme_dark_error,
    onError          = md_theme_dark_onError,
    errorContainer   = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,

    background   = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,

    surface          = md_theme_dark_surface,
    onSurface        = md_theme_dark_onSurface,
    surfaceVariant   = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,

    outline        = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,

    scrim            = md_theme_dark_scrim,
    inverseSurface   = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary   = md_theme_dark_inversePrimary,
    surfaceTint      = md_theme_dark_surfaceTint
)

/**
 * Корневая тема приложения SpyGame.
 *
 * На Android 12+ (API 31+) использует Dynamic Color (Material You):
 * цвета адаптируются под обои устройства пользователя.
 * На более старых устройствах — статическая indigo-палитра из [Color.kt].
 *
 * @param darkTheme     Используется ли тёмная тема. По умолчанию — системная настройка.
 * @param dynamicColor  Включены ли Dynamic Colors. По умолчанию — true (Android 12+).
 *                      Передайте false в Preview или тестах.
 * @param content       Compose-контент внутри темы.
 */
@Composable
fun SpyGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic Color работает только на Android 12+ (API 31)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else           dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
