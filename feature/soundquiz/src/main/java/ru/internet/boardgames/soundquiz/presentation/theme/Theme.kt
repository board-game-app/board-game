package ru.internet.boardgames.soundquiz.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Тема модуля :feature:sound-quiz.
 *
 * Приоритет цветов:
 * 1. Динамические цвета (Android 12+ / API 31+) — если [dynamicColor] = true.
 * 2. Статическая схема на базе seed #00897B (Teal) — для Android < 12.
 *
 * Используется только внутри soundQuizGraph — не влияет на тему :app
 * и других feature-модулей.
 *
 * @param darkTheme    следовать системной теме (по умолчанию).
 * @param dynamicColor включить Material You (по умолчанию true).
 * @param content      контент, оборачиваемый темой.
 */
@Composable
fun SoundQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Динамические цвета доступны начиная с Android 12 (API 31)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> SoundQuizDarkColorScheme
        else      -> SoundQuizLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SoundQuizTypography,
        content     = content
    )
}
