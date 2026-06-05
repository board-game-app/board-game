package ru.internet.boardgames.spygame.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import ru.internet.boardgames.spygame.presentation.game.GameScreen
import ru.internet.boardgames.spygame.presentation.settings.SettingsScreen
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/**
 * Публичный маршрут фичи — единственный идентификатор, который знает :app.
 *
 * Использование в корневом NavGraph (:app):
 * ```kotlin
 * NavHost(navController, startDestination = "home") {
 *     composable("home") { HomeScreen(onOpenSpyGame = { nav.navigate(SPY_GAME_ROUTE) }) }
 *     spyGameGraph(navController)
 * }
 * ```
 */
const val SPY_GAME_ROUTE = "spy_game"

// Внутренние маршруты — приватны, :app о них не знает
private const val GAME_ROUTE     = "spy_game/game"
private const val SETTINGS_ROUTE = "spy_game/settings"

/**
 * Точка входа в фичу SpyGame.
 *
 * Является расширением [NavGraphBuilder] — вызывается один раз
 * при построении корневого NavHost в :app.
 *
 * SpyGameTheme применяется здесь, чтобы:
 * - Экраны игры всегда получали правильную тему независимо от :app
 * - При добавлении Sound Quiz его тема не будет конфликтовать
 */
fun NavGraphBuilder.spyGameGraph(navController: NavHostController) {
    navigation(
        startDestination = GAME_ROUTE,
        route            = SPY_GAME_ROUTE
    ) {
        composable(GAME_ROUTE) {
            SpyGameTheme {
                GameScreen(
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) }
                )
            }
        }
        composable(SETTINGS_ROUTE) {
            SpyGameTheme {
                SettingsScreen(
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
