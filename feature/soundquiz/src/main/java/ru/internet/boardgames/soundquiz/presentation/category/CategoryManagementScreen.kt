package ru.internet.boardgames.soundquiz.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.soundquiz.R
import ru.internet.boardgames.soundquiz.domain.model.Category
import ru.internet.boardgames.soundquiz.presentation.theme.CardPaletteColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWords: (categoryId: Long) -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sound_quiz_categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sound_quiz_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onShowCreateDialog) {
                        Icon(Icons.Default.Add,
                            contentDescription = stringResource(R.string.sound_quiz_create_category))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(paddingValues)) {
                if (uiState.categories.isEmpty()) {
                    item {
                        Text(
                            text     = stringResource(R.string.sound_quiz_no_categories),
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryManagementItem(
                            category      = category,
                            onRowClick    = { onNavigateToWords(category.id) },
                            onEditClick   = { viewModel.onShowEditDialog(category) },
                            onDeleteClick = { viewModel.onDeleteRequest(category.id) }
                        )
                    }
                }
            }
        }
    }

    uiState.dialogState?.let { dialogState ->
        CategoryDialog(
            dialogState       = dialogState,
            onDismiss         = viewModel::onDialogDismiss,
            onNameChanged     = viewModel::onDialogNameChanged,
            onColorChanged    = viewModel::onDialogColorChanged,
            onConfirm         = viewModel::onDialogConfirm
        )
    }

    uiState.deleteConfirmationId?.let {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            title   = { Text(stringResource(R.string.sound_quiz_delete_category_title)) },
            text    = { Text(stringResource(R.string.sound_quiz_delete_category_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirm) {
                    Text(stringResource(R.string.sound_quiz_delete),
                        color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismiss) {
                    Text(stringResource(R.string.sound_quiz_cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryManagementItem(
    category: Category,
    onRowClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wordCountText = pluralStringResource(
        R.plurals.sound_quiz_word_count, category.wordCount, category.wordCount
    )
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(14.dp).background(Color(category.colorArgb), CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text  = stringResource(R.string.sound_quiz_category_subtitle, wordCountText, category.language.uppercase()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Нет разделения на встроенные/пользовательские —
        // все категории доступны для редактирования и удаления
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit,
                contentDescription = stringResource(R.string.sound_quiz_edit_category),
                modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete,
                contentDescription = stringResource(R.string.sound_quiz_delete_category),
                modifier = Modifier.size(20.dp),
                tint     = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    dialogState: CategoryDialogState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onColorChanged: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (dialogState) {
        is CategoryDialogState.Create -> stringResource(R.string.sound_quiz_dialog_new_category)
        is CategoryDialogState.Edit   -> stringResource(R.string.sound_quiz_dialog_edit_category)
    }
    val name = when (dialogState) {
        is CategoryDialogState.Create -> dialogState.name
        is CategoryDialogState.Edit   -> dialogState.name
    }
    val selectedColor = when (dialogState) {
        is CategoryDialogState.Create -> dialogState.selectedColorArgb
        is CategoryDialogState.Edit   -> dialogState.selectedColorArgb
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(title) },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = onNameChanged,
                    label         = { Text(stringResource(R.string.sound_quiz_dialog_name_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.sound_quiz_dialog_color),
                    style = MaterialTheme.typography.labelLarge)
                ColorPicker(selectedColorArgb = selectedColor, onColorSelected = onColorChanged)
                // Выбор языка убран — язык определяется из локали устройства
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.sound_quiz_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.sound_quiz_cancel)) }
        }
    )
}

@Composable
private fun ColorPicker(selectedColorArgb: Int, onColorSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        CardPaletteColors.forEach { color ->
            val argb       = color.toArgb()
            val isSelected = argb == selectedColorArgb
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, CircleShape)
                    .then(if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                    .clickable { onColorSelected(argb) }
            )
        }
    }
}
