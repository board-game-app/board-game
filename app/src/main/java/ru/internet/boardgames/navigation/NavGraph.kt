package ru.internet.boardgames.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.internet.boardgames.home.HomeScreen
import ru.internet.boardgames.spygame.presentation.navigation.SPY_GAME_ROUTE
import ru.internet.boardgames.spygame.presentation.navigation.spyGameGraph
import ru.internet.boardgames.ui.CounterSidePanel

// Импорты из :feature:counter
import ru.internet.boardgames.counter.presentation.navigation.counterGraph
import ru.internet.boardgames.counter.presentation.navigation.COUNTER_LIST_ROUTE
import ru.internet.boardgames.counter.presentation.navigation.CounterSheetContent

private const val HOME_ROUTE = "home"

/**
 * Корневой NavHost приложения «Настольные игры».
 *
 * Счётчик открывается тремя способами:
 * 1. Карточка Counter на HomeScreen    → navigate(COUNTER_LIST_ROUTE) — полный экран.
 * 2. Кнопка 🔢 в TopAppBar SpyGame    → CounterSidePanel (панель справа).
 * 3. Edge-свайп влево с правого края  → CounterSidePanel (панель справа).
 *
 * Пункты 2 и 3 недоступны:
 *   - На главном экране ([HOME_ROUTE]) — панель открывается только внутри игр.
 *   - На экранах Counter — исключает боковую панель поверх полноэкранного счётчика.
 *
 * Back-жест при открытой панели обрабатывается внутри [CounterSidePanel]:
 * первое нажатие — закрывает панель, второе — NavHost возвращает на HOME_ROUTE.
 */
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var showCounterPanel by rememberSaveable { mutableStateOf(false) }

    // Текущий маршрут — для управления доступностью edge-свайпа
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Edge-свайп и кнопка 🔢 доступны только внутри игровых экранов:
    //   • HOME_ROUTE  — панель не нужна на главном экране.
    //   • counter/*   — панель не открывается поверх полноэкранного счётчика.
    val edgeSwipeEnabled = !currentRoute.startsWith("counter") && currentRoute != HOME_ROUTE

    // BackHandler перенесён внутрь CounterSidePanel, где гарантирован
    // наивысший LIFO-приоритет (компонуется после всего содержимого NavHost).

    Box(modifier = modifier.fillMaxSize()) {

        NavHost(
            navController    = navController,
            startDestination = HOME_ROUTE,
            modifier         = Modifier.fillMaxSize()
        ) {
            // ── Главный экран ─────────────────────────────────────────────────
            composable(HOME_ROUTE) {
                HomeScreen(
                    onNavigateToSpyGame = { navController.navigate(SPY_GAME_ROUTE) },
                    onNavigateToCounter = { navController.navigate(COUNTER_LIST_ROUTE) }
                )
            }

            // ── SpyGame ───────────────────────────────────────────────────────
            spyGameGraph(
                navController = navController
            )

            // ── Counter (полный экран с HomeScreen) ───────────────────────────
            counterGraph(navController = navController)
        }

        // CounterSidePanel рендерится ПОСЛЕ NavHost → всегда поверх него.
        // Управление Back-жестом и закрытием инкапсулировано внутри компонента.
        CounterSidePanel(
            visible          = showCounterPanel,
            onDismiss        = { showCounterPanel = false },
            onOpen           = { showCounterPanel = true },
            edgeSwipeEnabled = edgeSwipeEnabled
        ) {
            CounterSheetContent()
        }
    }
}
