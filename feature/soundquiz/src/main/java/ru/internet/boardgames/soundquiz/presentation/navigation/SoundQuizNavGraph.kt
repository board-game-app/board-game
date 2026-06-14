package ru.internet.boardgames.soundquiz.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import ru.internet.boardgames.soundquiz.presentation.category.CategoryManagementScreen
import ru.internet.boardgames.soundquiz.presentation.category.WordManagementScreen
import ru.internet.boardgames.soundquiz.presentation.game.GameScreen
import ru.internet.boardgames.soundquiz.presentation.settings.SettingsScreen
import ru.internet.boardgames.soundquiz.presentation.theme.SoundQuizTheme

// ─────────────────────────────────────────────────────────────────────────────
// ПУБЛИЧНЫЙ API модуля :feature:sound-quiz
// ─────────────────────────────────────────────────────────────────────────────

/** Маршрут входной точки модуля — используется в :app для навигации */
const val SOUND_QUIZ_ROUTE = "sound_quiz"

// Внутренние маршруты (приватны для модуля)
private object Routes {
    const val GAME       = "sound_quiz/game"
    const val SETTINGS   = "sound_quiz/settings"
    const val CATEGORIES = "sound_quiz/categories"
    const val WORDS      = "sound_quiz/categories/{categoryId}/words"

    fun words(categoryId: Long) = "sound_quiz/categories/$categoryId/words"
}

/**
 * Регистрирует вложенный граф навигации модуля :feature:sound-quiz в NavHost.
 *
 * @param navController   Корневой контроллер навигации из :app.
 * :feature:sound-quiz не знает о :feature:counter — связь только через этот callback,
 * идентично тому как это сделано в :feature:spy-game.
 */
fun NavGraphBuilder.soundQuizGraph(
    navController: NavHostController
) {
    navigation(
        route            = SOUND_QUIZ_ROUTE,
        startDestination = Routes.GAME
    ) {

        // ── Игровой экран ────────────────────────────────────────────────────
        composable(route = Routes.GAME) {
            SoundQuizTheme {
                GameScreen(
                    onNavigateToSettings   = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToCategories = { navController.navigate(Routes.CATEGORIES) },
                )
            }
        }

        // ── Экран настроек ───────────────────────────────────────────────────
        composable(route = Routes.SETTINGS) {
            SoundQuizTheme {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ── Экран управления категориями ─────────────────────────────────────
        composable(route = Routes.CATEGORIES) {
            SoundQuizTheme {
                CategoryManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWords = { categoryId ->
                        navController.navigate(Routes.words(categoryId))
                    }
                )
            }
        }

        // ── Экран управления словами ─────────────────────────────────────────
        composable(
            route     = Routes.WORDS,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) {
            SoundQuizTheme {
                WordManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
