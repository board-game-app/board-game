package ru.internet.boardgames.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.internet.boardgames.home.HomeScreen
import ru.internet.boardgames.spygame.presentation.navigation.SPY_GAME_ROUTE
import ru.internet.boardgames.spygame.presentation.navigation.spyGameGraph

private const val HOME_ROUTE = "home"

/**
 * Корневой NavHost приложения «Настольные игры».
 *
 * Добавление новой игры — три шага:
 * 1. Импортировать SOUND_QUIZ_ROUTE и soundQuizGraph из feature-модуля
 * 2. Добавить soundQuizGraph(navController) сюда
 * 3. Передать лямбду onNavigateToSoundQuiz в HomeScreen
 */
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController  = navController,
        startDestination = HOME_ROUTE,
        modifier       = modifier
    ) {
        // ── Главный экран со списком игр ──────────────────────────────────
        composable(HOME_ROUTE) {
            HomeScreen(
                onNavigateToSpyGame = { navController.navigate(SPY_GAME_ROUTE) }
                // onNavigateToSoundQuiz = { navController.navigate(SOUND_QUIZ_ROUTE) }
            )
        }

        // ── Игры ─────────────────────────────────────────────────────────
        // Каждый feature-модуль предоставляет одну функцию-расширение NavGraphBuilder.
        // :app знает только публичный маршрут (SPY_GAME_ROUTE = "spy_game").
        spyGameGraph(navController)
        // soundQuizGraph(navController)
    }
}
