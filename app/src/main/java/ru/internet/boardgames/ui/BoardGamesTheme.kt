package ru.internet.boardgames.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Тема оболочки приложения — используется только для [HomeScreen].
 *
 * Намеренно минимальная: без кастомных цветов.
 * На Android 12+ — Dynamic Color (Material You).
 * На старых устройствах — дефолтная Material3 палитра.
 *
 * Каждая игра применяет свою тему в [SpyGameNavGraph] (и аналогичных).
 */
@Composable
fun BoardGamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else      -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}
