package ru.internet.boardgames.counter.presentation

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Безопасный поиск Activity в цепочке контекстов.
 *
 * [ModalBottomSheet] и [CounterSidePanel] оборачивают контекст в
 * [ContextThemeWrapper], поэтому прямой каст
 *   `LocalContext.current as ComponentActivity`
 * падает с ClassCastException. Эта функция поднимается по цепочке
 * [ContextWrapper.baseContext] до нахождения [ComponentActivity].
 */
private fun Context.findActivity(): ComponentActivity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    error("ComponentActivity не найдена в цепочке контекстов: $this")
}

/**
 * Тело счётчика для встраивания в CounterSidePanel (без TopAppBar).
 *
 * Разделяет тот же CounterViewModel (Activity scope) с CounterScreen —
 * состояние счётчиков единое в обоих режимах открытия.
 *
 * Изменения относительно оригинала:
 * - [findActivity()] вместо прямого каста (fix ClassCastException).
 * - Column получает fillMaxHeight() — контент заполняет всю панель.
 * - CounterListContent использует weight(1f) вместо heightIn(max=400dp) —
 *   список занимает доступное пространство, кнопка «Добавить» остаётся внизу.
 *
 * internal: публичный псевдоним [ru.internet.boardgames.counter.presentation.navigation.CounterSheetContent] — в CounterNavGraph.kt.
 */
@Composable
internal fun CounterSheetBodyContent(
    modifier: Modifier = Modifier,
    onNavigateToEditCounter: (() -> Unit)? = null
) {
    val activity = LocalContext.current.findActivity()
    val vm: CounterViewModel = hiltViewModel(activity)
    val uiState by vm.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()              // ← ИСПРАВЛЕНО: было без fillMaxHeight
            .navigationBarsPadding()
    ) {
        // ── Компактная строка сессии ──────────────────────────────────────────
        if (uiState.activeSession != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = uiState.activeSession!!.name,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.activeSessions.size > 1) {
                    TextButton(onClick = vm::showSessionPickerDialog) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Сменить")
                    }
                }
            }
            HorizontalDivider()
        }

        // ── Список счётчиков ──────────────────────────────────────────────────
        // weight(1f) занимает всё доступное пространство Column;
        // кнопка «Добавить» остаётся прижатой к низу панели.
        // Было: heightIn(max = 400.dp) — ограничивало до половины экрана.
        CounterListContent(
            uiState          = uiState,
            onCreateSession  = vm::showCreateSessionDialog,
            onIncrement      = { counter -> vm.applyDelta(counter.id, +counter.incrementStep) },
            onDecrement      = { counter -> vm.applyDelta(counter.id, -counter.decrementStep) },
            onTap = { counter ->
                if (onNavigateToEditCounter != null) {
                    vm.startEditExistingCounter(counter)
                    onNavigateToEditCounter()
                } else {
                    vm.requestResetCounter(counter)
                }
            },
            onLongPress = vm::requestResetCounter,
            onDeleteRequest = vm::requestDeleteCounter,
            onAction = { counter, delta -> vm.applyDelta(counter.id, delta) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)               // ← ИСПРАВЛЕНО: было heightIn(max = 400.dp)
        )

        // ── Кнопка «Добавить счётчик» ────────────────────────────────────────
        if (uiState.activeSession != null) {
            OutlinedButton(
                onClick  = vm::showNewCounterDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить счётчик")
            }
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
            sessions         = uiState.activeSessions,
            currentSessionId = uiState.activeSession?.id,
            onSelect         = vm::switchSession,
            onDismiss        = vm::hideSessionPickerDialog
        )
    }
    if (uiState.showNewCounterDialog) {
        NewCounterDialog(
            defaultName      = uiState.newCounterDefaultName,
            currentName      = uiState.newCounterDialogName,
            currentColorArgb = uiState.newCounterDialogColorArgb,
            onNameChange     = vm::onNewCounterDialogNameChange,
            onColorChange    = vm::onNewCounterDialogColorChange,
            onConfirm        = vm::createCounterFromDialog,
            onDismiss        = vm::hideNewCounterDialog,
            onExpandToEditor = onNavigateToEditCounter?.let {
                {
                    vm.startNewCounterFromDialog()
                    it()
                }
            }
        )
    }
    uiState.counterPendingReset?.let { counter ->
        ResetConfirmDialog(
            counter   = counter,
            onConfirm = vm::confirmResetCounter,
            onDismiss = vm::dismissResetDialog
        )
    }
    uiState.counterPendingDelete?.let { counter ->
        DeleteConfirmDialog(
            counter   = counter,
            onConfirm = vm::confirmDeleteCounter,
            onDismiss = vm::dismissDeleteDialog
        )
    }
}
