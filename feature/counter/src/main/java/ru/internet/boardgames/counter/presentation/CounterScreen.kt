package ru.internet.boardgames.counter.presentation

import ru.internet.boardgames.counter.presentation.utils.findActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.presentation.navigation.COUNTER_EDIT_ROUTE

/**
 * Полноэкранный главный экран счётчика (§4 ТЗ).
 *
 * AppBar: гамбургер (слева) · «Все счётчики» (центр) · тоггл режима (справа).
 * Список: LazyVerticalGrid (2 колонки) для COMPACT, LazyColumn для WIDE.
 * FAB: открывает диалог «Новый счётчик» (§6).
 *
 * Изменение 1 (инверсия жестов):
 *   • Тап на карточку → перейти на EditCounterScreen.
 *   • Долгое нажатие → диалог сброса до resetValue.
 *
 * Изменение 4 (drawer):
 *   • Гамбургер открывает [SessionDrawer] (ModalNavigationDrawer).
 *   • Drawer синхронизируется с [CounterUiState.isDrawerOpen].
 *
 * internal: публичный вход — только через counterGraph в CounterNavGraph.kt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CounterScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val vm: CounterViewModel = hiltViewModel(activity)
    val uiState by vm.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ── Синхронизация VM → drawer UI ─────────────────────────────────────────
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) drawerState.open()
        else if (drawerState.isOpen) drawerState.close()
    }

    // ── Синхронизация gesture-закрытие drawer → VM ────────────────────────────
    // Когда пользователь закрывает drawer свайпом — сбрасываем флаг в VM
    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isClosed }.collect { isClosed ->
            if (isClosed) vm.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SessionDrawer(
                sessions = uiState.activeSessions,
                activeSessionId = uiState.activeSession?.id,
                renamingSessionId = uiState.renamingSessionId,
                renamingSessionName = uiState.renamingSessionName,
                onSessionClick = { sessionId ->
                    vm.switchSession(sessionId)
                    scope.launch { drawerState.close() }
                    vm.closeDrawer()
                },
                onStartRename = vm::startRenameSession,
                onRenameNameChange = vm::onRenameSessionNameChange,
                onConfirmRename = vm::confirmRenameSession,
                onCancelRename = vm::cancelRenameSession,
                onDeleteRequest = vm::requestDeleteSession,
                onCreateSession = {
                    vm.closeDrawer()
                    vm.showCreateSessionDialog()
                }
            )
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        // Гамбургер — открывает SessionDrawer (Изменение 4)
                        IconButton(onClick = vm::openDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Управление сессиями"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = uiState.activeSession?.name ?: "Все счётчики",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        // Тоггл режима карточек (§4.1, §5, §11)
                        IconButton(onClick = vm::toggleCardDisplayMode) {
                            Icon(
                                imageVector = if (uiState.cardDisplayMode == CardDisplayMode.COMPACT)
                                    Icons.Default.ViewAgenda
                                else
                                    Icons.Default.GridView,
                                contentDescription = if (uiState.cardDisplayMode == CardDisplayMode.COMPACT)
                                    "Широкий вид"
                                else
                                    "Компактный вид"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (uiState.activeSession != null) {
                    FloatingActionButton(onClick = vm::showNewCounterDialog) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить счётчик"
                        )
                    }
                }
            }
        ) { paddingValues ->
            CounterListContent(
                uiState = uiState,
                onCreateSession = vm::showCreateSessionDialog,
                onIncrement = { counter ->
                    vm.applyDelta(counter.id, +counter.incrementStep)
                },
                onDecrement = { counter ->
                    vm.applyDelta(counter.id, -counter.decrementStep)
                },
                // Изменение 1: тап → перейти на EditCounterScreen
                onTap = { counter ->
                    vm.startEditExistingCounter(counter)
                    navController.navigate(COUNTER_EDIT_ROUTE)
                },
                // Изменение 1: долгое нажатие → диалог сброса
                onLongPress = { counter ->
                    vm.requestResetCounter(counter)
                },
                onDeleteRequest = vm::requestDeleteCounter,
                onAction = { counter, delta -> vm.applyDelta(counter.id, delta) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    // ── Диалоги ───────────────────────────────────────────────────────────────

    if (uiState.showCreateSessionDialog) {
        CreateSessionDialog(
            onConfirm = vm::createSession,
            onDismiss = vm::hideCreateSessionDialog
        )
    }
    if (uiState.showSessionPickerDialog) {
        SessionPickerDialog(
            sessions = uiState.activeSessions,
            currentSessionId = uiState.activeSession?.id,
            onSelect = vm::switchSession,
            onDismiss = vm::hideSessionPickerDialog
        )
    }
    if (uiState.showNewCounterDialog) {
        NewCounterDialog(
            defaultName = uiState.newCounterDefaultName,
            currentName = uiState.newCounterDialogName,
            currentColorArgb = uiState.newCounterDialogColorArgb,
            onNameChange = vm::onNewCounterDialogNameChange,
            onColorChange = vm::onNewCounterDialogColorChange,
            onConfirm = vm::createCounterFromDialog,
            onDismiss = vm::hideNewCounterDialog,
            onExpandToEditor = {
                vm.startNewCounterFromDialog()
                navController.navigate(COUNTER_EDIT_ROUTE)
            }
        )
    }
    uiState.counterPendingReset?.let { counter ->
        ResetConfirmDialog(
            counter = counter,
            onConfirm = vm::confirmResetCounter,
            onDismiss = vm::dismissResetDialog
        )
    }
    uiState.counterPendingDelete?.let { counter ->
        DeleteConfirmDialog(
            counter = counter,
            onConfirm = vm::confirmDeleteCounter,
            onDismiss = vm::dismissDeleteDialog
        )
    }
    // Изменение 4: диалог подтверждения удаления сессии
    uiState.sessionPendingDelete?.let { session ->
        DeleteSessionConfirmDialog(
            session = session,
            onConfirm = vm::confirmDeleteSession,
            onDismiss = vm::dismissDeleteSessionDialog
        )
    }
}

// ── Контент списка ────────────────────────────────────────────────────────────

/**
 * Переключает между [CompactCounterGrid] и [WideCounterList] в зависимости от
 * [CounterUiState.cardDisplayMode], а также обрабатывает состояния загрузки и пустоты.
 *
 * Изменение 1: коллбэк переименован [onTap] (тап → редактировать),
 * [onLongPress] теперь инициирует сброс.
 */
@Composable
internal fun CounterListContent(
    uiState: CounterUiState,
    onCreateSession: () -> Unit,
    onIncrement: (Counter) -> Unit,
    onDecrement: (Counter) -> Unit,
    onTap: (Counter) -> Unit,
    onLongPress: (Counter) -> Unit,
    onDeleteRequest: (Counter) -> Unit,
    onAction: (Counter, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.activeSession == null -> {
            EmptySessionsPlaceholder(
                onCreateSession = onCreateSession,
                modifier = modifier
            )
        }

        uiState.counters.isEmpty() -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "Нажмите + чтобы добавить счётчик",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        uiState.cardDisplayMode == CardDisplayMode.COMPACT -> {
            CompactCounterGrid(
                counters = uiState.counters,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onTap = onTap,
                onLongPress = onLongPress,
                onDeleteRequest = onDeleteRequest,
                onAction = onAction,
                modifier = modifier
            )
        }

        else -> {
            WideCounterList(
                counters = uiState.counters,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onTap = onTap,
                onLongPress = onLongPress,
                onDeleteRequest = onDeleteRequest,
                onAction = onAction,
                modifier = modifier
            )
        }
    }
}

// ── Компактная сетка 2 колонки (§5.1) ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactCounterGrid(
    counters: List<Counter>,
    onIncrement: (Counter) -> Unit,
    onDecrement: (Counter) -> Unit,
    onTap: (Counter) -> Unit,
    onLongPress: (Counter) -> Unit,
    onDeleteRequest: (Counter) -> Unit,
    onAction: (Counter, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = counters, key = { it.id }) { counter ->
            SwipeToDeleteWrapper(
                onDeleteRequest = { onDeleteRequest(counter) }
            ) {
                CompactCounterCard(
                    counter = counter,
                    onIncrement = { onIncrement(counter) },
                    onDecrement = { onDecrement(counter) },
                    onTap = { onTap(counter) },
                    onLongPress = { onLongPress(counter) },
                    onDeleteRequest = { onDeleteRequest(counter) },
                    onAction = { delta -> onAction(counter, delta) }
                )
            }
        }
    }
}

// ── Широкий список (§5.2) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WideCounterList(
    counters: List<Counter>,
    onIncrement: (Counter) -> Unit,
    onDecrement: (Counter) -> Unit,
    onTap: (Counter) -> Unit,
    onLongPress: (Counter) -> Unit,
    onDeleteRequest: (Counter) -> Unit,
    onAction: (Counter, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = counters, key = { it.id }) { counter ->
            SwipeToDeleteWrapper(
                onDeleteRequest = { onDeleteRequest(counter) }
            ) {
                WideCounterCard(
                    counter = counter,
                    onIncrement = { onIncrement(counter) },
                    onDecrement = { onDecrement(counter) },
                    onTap = { onTap(counter) },
                    onLongPress = { onLongPress(counter) },
                    onDeleteRequest = { onDeleteRequest(counter) },
                    onAction = { delta -> onAction(counter, delta) }
                )
            }
        }
    }
}

// ── Обёртка свайп-удалить (§5.4) ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWrapper(
    onDeleteRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // При достижении порога — запрашиваем подтверждение и возвращаем false (snap back)
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Красный фон с иконкой удаления при свайпе влево
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        content = { content() }
    )
}

// ── Пустое состояние ──────────────────────────────────────────────────────────

@Composable
private fun EmptySessionsPlaceholder(
    onCreateSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Нет ни одной сессии", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Создайте первую сессию, чтобы начать считать",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onCreateSession) {
            Text("Создать сессию")
        }
    }
}

// ── Диалоги (internal — переиспользуются в CounterSheetBody) ──────────────────

@Composable
internal fun CreateSessionDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая сессия") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                placeholder = { Text("Например: Вечер пятницы") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Создать")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
internal fun SessionPickerDialog(
    sessions: List<Session>,
    currentSessionId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать сессию") },
        text = {
            Column {
                sessions.forEach { session ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = session.id == currentSessionId,
                            onClick = { onSelect(session.id) }
                        )
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
internal fun ResetConfirmDialog(
    counter: Counter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сбросить счётчик?") },
        text = {
            Text(
                "Сбросить «${counter.name}» до ${counter.resetValue}?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Сбросить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
internal fun DeleteConfirmDialog(
    counter: Counter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить счётчик?") },
        text = {
            Text(
                "Удалить счётчик «${counter.name}»? Это действие нельзя отменить.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Удалить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

/**
 * Диалог подтверждения удаления сессии.
 * Изменение 4: используется из drawer.
 */
@Composable
internal fun DeleteSessionConfirmDialog(
    session: Session,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить сессию?") },
        text = {
            Text(
                "Удалить сессию «${session.name}»? Все счётчики будут удалены.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Удалить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
