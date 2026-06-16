package ru.internet.boardgames.counter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.internet.boardgames.counter.presentation.CounterScreen
import ru.internet.boardgames.counter.presentation.CounterSheetBodyContent
import ru.internet.boardgames.counter.presentation.EditCounterScreen

const val COUNTER_LIST_ROUTE = "counter_list"
const val COUNTER_EDIT_ROUTE = "counter_edit"

private const val SHEET_LIST_ROUTE = "sheet_list"
private const val SHEET_EDIT_ROUTE = "sheet_edit"

fun NavGraphBuilder.counterGraph(navController: NavHostController) {
    composable(route = COUNTER_LIST_ROUTE) {
        CounterScreen(navController = navController)
    }
    composable(route = COUNTER_EDIT_ROUTE) {
        EditCounterScreen(navController = navController)
    }
}

/**
 * @param isActive true — панель видима, диалоги показываются.
 *                 false — панель скрыта, диалоги подавляются.
 *
 * CounterSheetContent всегда остаётся в композиции (для анимаций),
 * но при isActive = false все диалоги внутри CounterSheetBodyContent
 * не отображаются, исключая конкуренцию с диалогами CounterScreen.
 */
@Composable
fun CounterSheetContent(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val sheetNavController = rememberNavController()

    NavHost(
        navController    = sheetNavController,
        startDestination = SHEET_LIST_ROUTE,
        modifier         = modifier
    ) {
        composable(route = SHEET_LIST_ROUTE) {
            CounterSheetBodyContent(
                isActive = isActive,
                onNavigateToEditCounter = {
                    sheetNavController.navigate(SHEET_EDIT_ROUTE)
                }
            )
        }
        composable(route = SHEET_EDIT_ROUTE) {
            EditCounterScreen(navController = sheetNavController)
        }
    }
}
