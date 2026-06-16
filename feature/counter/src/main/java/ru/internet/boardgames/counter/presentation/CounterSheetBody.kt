package ru.internet.boardgames.counter.presentation

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private const val TAG = "CounterNav"

private fun Context.findActivity(): ComponentActivity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    error("ComponentActivity не найдена: $this")
}

/**
 * @param isActive Управляет видимостью диалогов.
 *   true (по умолчанию) — панель/шторка видима, диалоги отображаются нормально.
 *   false — компонент в композиции, но скрыт; все диалоги подавляются,
 *   чтобы не конкурировать с диалогами CounterScreen, подписанного
 *   на тот же ViewModel.
 */
@Composable
internal fun CounterSheetBodyContent(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onNavigateToEditCounter: (() -> Unit)? = null
) {
    val activity = LocalContext.current.findActivity()
    val vm: CounterViewModel = hiltViewModel(activity)
    val uiState by vm.uiState.collectAsState()

    var navigateToEditorPending by remember { mutableStateOf(false) }
    LaunchedEffect(navigateToEditorPending) {
        if (navigateToEditorPending) {
            navigateToEditorPending = false
            Log.d(TAG, "[Sheet] Invoking onNavigateToEditCounter")
            onNavigateToEditCounter?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .navigationBarsPadding()
    ) {
        if (uiState.activeSession != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.activeSession!!.name,
                    style = MaterialTheme.typography.titleMedium,
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

        CounterListContent(
            uiState         = uiState,
            onCreateSession = vm::showCreateSessionDialog,
            onIncrement     = { counter -> vm.applyDelta(counter.id, +counter.incrementStep) },
            onDecrement     = { counter -> vm.applyDelta(counter.id, -counter.decrementStep) },
            onTap = { counter ->
                if (onNavigateToEditCounter != null) {
                    vm.startEditExistingCounter(counter)
                    onNavigateToEditCounter()
                } else {
                    vm.requestResetCounter(counter)
                }
            },
            onLongPress     = vm::requestResetCounter,
            onDeleteRequest = vm::requestDeleteCounter,
            onAction        = { counter, delta -> vm.applyDelta(counter.id, delta) },
            modifier        = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

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

    // ── Диалоги — показываются только когда панель активна (isActive = true) ──
    // Когда isActive = false, CounterScreen и CounterSheetBodyContent подписаны
    // на один ViewModel и видят одно состояние. Без этой проверки оба компонента
    // показывают диалог одновременно, и пользователь случайно нажимает
    // на «невидимый» диалог шторки вместо диалога полного экрана.
    if (isActive) {
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
                onExpandToEditor = if (onNavigateToEditCounter != null) {
                    {
                        Log.d(TAG, "[Sheet] onExpandToEditor called")
                        vm.startNewCounterFromDialog()
                        navigateToEditorPending = true
                    }
                } else null
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
}
