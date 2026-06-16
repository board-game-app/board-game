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
import ru.internet.boardgames.soundquiz.presentation.navigation.SOUND_QUIZ_ROUTE
import ru.internet.boardgames.soundquiz.presentation.navigation.soundQuizGraph
import ru.internet.boardgames.ui.CounterSidePanel
import ru.internet.boardgames.counter.presentation.navigation.counterGraph
import ru.internet.boardgames.counter.presentation.navigation.COUNTER_LIST_ROUTE
import ru.internet.boardgames.counter.presentation.navigation.CounterSheetContent

private const val HOME_ROUTE = "home"

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var showCounterPanel by rememberSaveable { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val edgeSwipeEnabled = !currentRoute.startsWith("counter") && currentRoute != HOME_ROUTE

    Box(modifier = modifier.fillMaxSize()) {

        NavHost(
            navController    = navController,
            startDestination = HOME_ROUTE,
            modifier         = Modifier.fillMaxSize()
        ) {
            composable(HOME_ROUTE) {
                HomeScreen(
                    onNavigateToSpyGame   = { navController.navigate(SPY_GAME_ROUTE) },
                    onNavigateToSoundQuiz = { navController.navigate(SOUND_QUIZ_ROUTE) },
                    onNavigateToCounter   = { navController.navigate(COUNTER_LIST_ROUTE) }
                )
            }
            spyGameGraph(navController = navController)
            soundQuizGraph(navController = navController)
            counterGraph(navController = navController)
        }

        CounterSidePanel(
            visible          = showCounterPanel,
            onDismiss        = { showCounterPanel = false },
            onOpen           = { showCounterPanel = true },
            edgeSwipeEnabled = edgeSwipeEnabled
        ) {
            // isActive = showCounterPanel:
            // CounterSheetContent всегда в композиции (нужно для анимации открытия/закрытия),
            // но диалоги внутри него показываются только когда панель видима.
            // Это предотвращает ситуацию когда CounterScreen и CounterSheetBodyContent
            // одновременно показывают NewCounterDialog на один и тот же уiState.
            CounterSheetContent(isActive = showCounterPanel)
        }
    }
}
