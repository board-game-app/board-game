package ru.internet.boardgames.spygame.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import ru.internet.boardgames.spygame.presentation.game.GameScreen
import ru.internet.boardgames.spygame.presentation.settings.SettingsScreen
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

const val SPY_GAME_ROUTE = "spy_game"

private const val GAME_ROUTE     = "spy_game/game"
private const val SETTINGS_ROUTE = "spy_game/settings"

/**
 * Публичный API фичи SpyGame для корневого NavGraph в :app.
 *
 * @param navController  Корневой контроллер навигации.
 * @param onOpenCounter  Вызывается при нажатии иконки 🔢 в TopAppBar.
 *                       null (по умолчанию) — иконка не отображается.
 *                       :app передаёт { showCounterSheet = true }.
 *
 * :feature:spy-game не знает о :feature:counter и не импортирует из него ничего.
 * Вся связь — через этот nullable-callback.
 */
fun NavGraphBuilder.spyGameGraph(
    navController: NavHostController
) {
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
                SettingsScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}
