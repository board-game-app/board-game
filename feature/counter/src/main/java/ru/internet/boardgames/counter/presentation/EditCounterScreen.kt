package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/**
 * Экран создания и редактирования счётчика (§7 ТЗ).
 *
 * Исправление 1: получает Activity через [findActivity] вместо прямого каста.
 * Исправление 2: поле «Действия» (§7.5) использует числовую клавиатуру
 *   [KeyboardType.Number] с [ImeAction.Done] для скрытия клавиатуры.
 *
 * @param navController  NavController для возврата назад (popBackStack).
 *   В шторке это внутренний NavController CounterSheetContent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditCounterScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Исправление 1: безопасный обход цепочки контекстов
    val activity = LocalContext.current.findActivity()
    val vm: CounterViewModel = hiltViewModel(activity)
    val uiState by vm.uiState.collectAsState()
    val editState = uiState.editState

    // Исправление 2: контроллер клавиатуры для ImeAction.Done
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    // Кнопка «✕» — закрыть; проверяем несохранённые изменения (§11)
                    IconButton(onClick = {
                        if (editState.hasUnsavedChanges) {
                            vm.requestDiscardChanges()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть без сохранения"
                        )
                    }
                },
                title = {
                    Text(
                        text = if (editState.isNewCounter) "Новый счётчик" else "Редактировать"
                    )
                },
                actions = {
                    // Кнопка «✓» — сохранить
                    IconButton(
                        onClick = {
                            if (vm.saveEditCounter()) navController.popBackStack()
                        },
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
                // Исправление: imePadding() ДО verticalScroll — видимая область
                // скроллируемого контента уменьшается на высоту клавиатуры, и Compose
                // автоматически подскроллит до сфокусированного поля.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── §7.1 Название ─────────────────────────────────────────────────
            OutlinedTextField(
                value = editState.name,
                onValueChange = vm::onEditNameChange,
                label = { Text("Название") },
                placeholder = { Text("Например: Очки игрока 1") },
                singleLine = true,
                // Исправление: Done закрывает клавиатуру; пользователь сам тапает
                // следующее поле — более естественный UX для формы с опциональными полями.
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── §7.2 Текущее значение ─────────────────────────────────────────
            OutlinedTextField(
                value = editState.valueText,
                onValueChange = vm::onEditValueChange,
                label = { Text("Текущее значение") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── §7.2 Значение сброса ──────────────────────────────────────────
            OutlinedTextField(
                value = editState.resetValueText,
                onValueChange = vm::onEditResetValueChange,
                label = { Text("Значение сброса") },
                supportingText = { Text("Долгое нажатие на карточку сбросит счётчик до этого значения") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── §7.3 Цвет счётчика ────────────────────────────────────────────
            Text(
                text = "Цвет",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            ColorPickerRow(
                selectedColorArgb = editState.colorArgb,
                onColorSelected = vm::onEditColorChange,
                modifier = Modifier.fillMaxWidth()
            )

            // ── §7.4 Шаги инкремента / декремента ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editState.incrementStepText,
                    onValueChange = vm::onEditIncrementStepChange,
                    label = { Text("Шаг +") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = editState.decrementStepText,
                    onValueChange = vm::onEditDecrementStepChange,
                    label = { Text("Шаг −") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── §7.5 Быстрые действия ─────────────────────────────────────────
            // Исправление 2: числовая клавиатура + ImeAction.Done с hideKeyboard
            OutlinedTextField(
                value = editState.actionsRaw,
                onValueChange = vm::onEditActionsRawChange,
                label = { Text("Быстрые действия") },
                placeholder = { Text("Например: 10 -5 20") },
                supportingText = {
                    Text("Числа через пробел. Тап на кнопку быстрого действия прибавляет (или убавляет) указанное значение.")
                },
                singleLine = true,
                // Исправление 2
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                trailingIcon = if (editState.actionsRaw.isNotEmpty()) {
                    {
                        IconButton(onClick = vm::clearEditActions) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Диалог «Отменить изменения?» (§11) ───────────────────────────────────
    if (editState.showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = vm::dismissDiscardChangesDialog,
            title = { Text("Отменить изменения?") },
            text = { Text("Несохранённые изменения будут потеряны.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.dismissDiscardChangesDialog()
                        navController.popBackStack()
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

