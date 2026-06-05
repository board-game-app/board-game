package ru.internet.boardgames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import ru.internet.boardgames.navigation.NavGraph
import ru.internet.boardgames.ui.BoardGamesTheme

/**
 * Единственная Activity проекта.
 *
 * Применяет [BoardGamesTheme] — минимальную Material3-тему для HomeScreen.
 * Каждая игра применяет собственную тему внутри [NavGraph]
 * (см. [SpyGameNavGraph]).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BoardGamesTheme {
                NavGraph()
            }
        }
    }
}
