package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @param onExpandToEditor Коллбэк кнопки «Развернуть».
 *   null → кнопка не отображается (например, когда вызывающий экран
 *   не может обеспечить навигацию на EditCounterScreen).
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Новый счётчик",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (onExpandToEditor != null) {
                    IconButton(onClick = onExpandToEditor) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Открыть полный редактор"
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    label = { Text("Имя") },
                    placeholder = { Text(defaultName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
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
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("ОК") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
