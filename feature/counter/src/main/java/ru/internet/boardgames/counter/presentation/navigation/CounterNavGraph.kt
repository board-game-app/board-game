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

// ── Маршруты публичного графа ─────────────────────────────────────────────────

/** Маршрут главного экрана счётчика */
const val COUNTER_LIST_ROUTE = "counter_list"

/** Маршрут экрана редактирования/создания счётчика */
const val COUNTER_EDIT_ROUTE = "counter_edit"

// ── Внутренние маршруты шторки ────────────────────────────────────────────────
// Используются только внутри CounterSheetContent — изолированы от основного NavHost.

/** Начальный экран внутри шторки — список счётчиков */
private const val SHEET_LIST_ROUTE = "sheet_list"

/** Экран редактирования внутри шторки */
private const val SHEET_EDIT_ROUTE = "sheet_edit"

// ── Граф фичи (встраивается в основной NavHost приложения) ───────────────────

/**
 * Добавляет в граф навигации приложения два маршрута фичи «Счётчик»:
 * [COUNTER_LIST_ROUTE] и [COUNTER_EDIT_ROUTE].
 *
 * @param navController NavController основного приложения.
 */
fun NavGraphBuilder.counterGraph(navController: NavHostController) {
    composable(route = COUNTER_LIST_ROUTE) {
        CounterScreen(navController = navController)
    }
    composable(route = COUNTER_EDIT_ROUTE) {
        EditCounterScreen(navController = navController)
    }
}

// ── CounterSheetContent — публичный composable для BottomSheet / панели ───────

/**
 * Публичная точка входа для встраивания счётчика в ModalBottomSheet или боковую панель.
 *
 * Исправление 3: содержит собственный [NavHost] с маршрутами [SHEET_LIST_ROUTE] и
 * [SHEET_EDIT_ROUTE]. Переход в [EditCounterScreen] происходит **внутри** шторки,
 * а не поверх всего приложения, как было при вызове основного navController.
 *
 * Использование в :app — без параметров:
 * ```kotlin
 * // Было:
 * CounterSheetContent(
 *     onNavigateToEditCounter = { navController.navigate(COUNTER_EDIT_ROUTE) }
 * )
 * // Стало:
 * CounterSheetContent()
 * ```
 */
@Composable
fun CounterSheetContent(modifier: Modifier = Modifier) {
    val sheetNavController = rememberNavController()

    NavHost(
        navController = sheetNavController,
        startDestination = SHEET_LIST_ROUTE,
        modifier = modifier
    ) {
        composable(route = SHEET_LIST_ROUTE) {
            CounterSheetBodyContent(
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
