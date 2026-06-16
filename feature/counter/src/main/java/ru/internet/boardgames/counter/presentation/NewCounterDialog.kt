package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Диалог быстрого создания счётчика.
 *
 * Использует [Dialog] + [Surface] вместо [AlertDialog]:
 * AlertDialog оборачивает слот `text` в verticalScroll-контейнер,
 * из-за чего первый тап на кнопки внутри поглощается детектором
 * скролла, а не самой кнопкой — пользователь вынужден нажимать дважды.
 * Кастомный Dialog лишён этой обёртки.
 *
 * Отличия от оригинального приложения:
 *  – иконка «развернуть» убрана из заголовка; вместо неё —
 *    текстовая кнопка «Подробнее →» в теле диалога под палитрой;
 *  – палитра цветов — сетка 2 × 5 вместо горизонтального ряда;
 *  – кнопка подтверждения называется «Создать», а не «ОК».
 *
 * Порядок элементов сохранён: сначала поле имени, затем палитра.
 */
@Composable
internal fun NewCounterDialog(
    defaultName: String,
    currentName: String,
    currentColorArgb: Long,
    onNameChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onExpandToEditor: (() -> Unit)?
) {
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // ── Заголовок ─────────────────────────────────────────────────
                Text(
                    text = "Новый счётчик",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ── Поле имени ────────────────────────────────────────────────
                OutlinedTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    label = { Text("Имя") },
                    placeholder = { Text(defaultName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Палитра цветов (сетка 2×5) ────────────────────────────────
                Text(
                    text = "Цвет",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ColorPickerRow(
                    selectedColorArgb = currentColorArgb,
                    onColorSelected = onColorChange,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Кнопка «Подробнее» (была иконкой в заголовке) ────────────
                if (onExpandToEditor != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            // Убираем фокус с TextField до перехода,
                            // чтобы клавиатура закрылась корректно
                            focusManager.clearFocus()
                            onExpandToEditor()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Подробнее →")
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Кнопки «Отмена» / «Создать» ──────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("Создать")
                    }
                }
            }
        }
    }
}
