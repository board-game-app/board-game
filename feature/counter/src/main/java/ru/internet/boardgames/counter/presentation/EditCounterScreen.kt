package ru.internet.boardgames.counter.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ru.internet.boardgames.counter.presentation.utils.findActivity

private const val TAG = "CounterNav"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditCounterScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val vm: CounterViewModel = hiltViewModel(activity)
    val uiState by vm.uiState.collectAsState()
    val editState = uiState.editState
    val keyboardController = LocalSoftwareKeyboardController.current

    var newActionText by remember { mutableStateOf("") }
    val actionValues = remember(editState.actionsRaw) {
        editState.actionsRaw.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
    }

    // ── Отложенный popBackStack (аналог fix'а в CounterScreen) ───────────────
    var popBackStackPending by remember { mutableStateOf(false) }
    LaunchedEffect(popBackStackPending) {
        Log.d(TAG, "[Edit] LaunchedEffect triggered: popBackStackPending=$popBackStackPending")
        if (popBackStackPending) {
            val entry = navController.currentBackStackEntry
            Log.d(TAG, "[Edit] BackStackEntry: route=${entry?.destination?.route}, " +
                    "lifecycleState=${entry?.lifecycle?.currentState}")
            popBackStackPending = false
            Log.d(TAG, "[Edit] Calling navController.popBackStack()")
            val result = navController.popBackStack()
            Log.d(TAG, "[Edit] popBackStack() returned: $result")
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(TAG, "[Edit] X clicked. hasUnsavedChanges=${editState.hasUnsavedChanges}")
                        if (editState.hasUnsavedChanges) {
                            vm.requestDiscardChanges()
                        } else {
                            val entry = navController.currentBackStackEntry
                            Log.d(TAG, "[Edit] Direct popBackStack. " +
                                    "lifecycleState=${entry?.lifecycle?.currentState}")
                            val result = navController.popBackStack()
                            Log.d(TAG, "[Edit] popBackStack() returned: $result")
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть без сохранения")
                    }
                },
                title = {
                    Text(if (editState.isNewCounter) "Новый счётчик" else "Редактировать")
                },
                actions = {
                    IconButton(
                        onClick = { if (vm.saveEditCounter()) navController.popBackStack() },
                        enabled = editState.name.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = if (editState.name.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editState.name,
                onValueChange = vm::onEditNameChange,
                label = { Text("Название") },
                placeholder = { Text("Например: Очки игрока 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editState.valueText,
                onValueChange = vm::onEditValueChange,
                label = { Text("Текущее значение") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editState.resetValueText,
                onValueChange = vm::onEditResetValueChange,
                label = { Text("Значение сброса") },
                supportingText = { Text("Долгое нажатие на карточку сбросит счётчик до этого значения") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Цвет", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp))
            ColorPickerRow(selectedColorArgb = editState.colorArgb, onColorSelected = vm::onEditColorChange,
                modifier = Modifier.fillMaxWidth())

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Шаги", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Кнопка «+»", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editState.incrementStepText, onValueChange = vm::onEditIncrementStepChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier.width(88.dp))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Text("Кнопка «−»", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editState.decrementStepText, onValueChange = vm::onEditDecrementStepChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier.width(88.dp))
                    }
                }
            }

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Быстрые действия", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (actionValues.isEmpty()) {
                        Text("Нет добавленных действий", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            actionValues.forEachIndexed { index, value ->
                                ActionEditorChip(value = value, onRemove = {
                                    val updated = actionValues.toMutableList().also { it.removeAt(index) }
                                    vm.onEditActionsRawChange(updated.joinToString(" "))
                                })
                            }
                        }
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newActionText, onValueChange = { newActionText = it },
                            label = { Text("Значение") }, placeholder = { Text("Напр. −5") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val v = newActionText.toIntOrNull()
                                if (v != null) { vm.onEditActionsRawChange((actionValues + v).joinToString(" ")); newActionText = "" }
                            }),
                            modifier = Modifier.weight(1f))
                        Button(onClick = {
                            val v = newActionText.toIntOrNull()
                            if (v != null) { vm.onEditActionsRawChange((actionValues + v).joinToString(" ")); newActionText = ""; keyboardController?.hide() }
                        }, enabled = newActionText.toIntOrNull() != null) { Text("Добавить") }
                    }
                    Text("Тап на кнопку быстрого действия прибавляет (или убавляет) указанное значение.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Диалог «Отменить изменения?» ──────────────────────────────────────────
    if (editState.showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = vm::dismissDiscardChangesDialog,
            title = { Text("Отменить изменения?") },
            text = { Text("Несохранённые изменения будут потеряны.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "[Edit] 'Отменить' clicked in discard dialog. " +
                                "BackStackEntry: route=${navController.currentBackStackEntry?.destination?.route}, " +
                                "lifecycleState=${navController.currentBackStackEntry?.lifecycle?.currentState}")
                        vm.dismissDiscardChangesDialog()
                        Log.d(TAG, "[Edit] dismissDiscardChangesDialog() done. " +
                                "Setting popBackStackPending=true")
                        popBackStackPending = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Отменить") }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissDiscardChangesDialog) {
                    Text("Продолжить редактирование")
                }
            }
        )
    }
}

@Composable
private fun ActionEditorChip(
    value: Int,
    onRemove: () -> Unit
) {
    val label = if (value >= 0) "+$value" else "$value"
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(2.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить действие $label",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
