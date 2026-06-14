package ru.internet.boardgames.soundquiz.presentation.category

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.internet.boardgames.soundquiz.R
import ru.internet.boardgames.soundquiz.domain.model.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: WordManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wordsTitle = stringResource(R.string.sound_quiz_words_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.categoryName.ifEmpty { wordsTitle },
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sound_quiz_back))
                    }
                },
                actions = {
                    // Кнопка «+» доступна для всех категорий (встроенные тоже редактируемы)
                    IconButton(onClick = viewModel::onShowAddDialog) {
                        Icon(Icons.Default.Add,
                            contentDescription = stringResource(R.string.sound_quiz_add_word))
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
                if (uiState.words.isEmpty()) {
                    item {
                        Text(
                            text     = stringResource(R.string.sound_quiz_add_first_word),
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    items(uiState.words, key = { it.id }) { word ->
                        // CRUD доступен для всех слов без разделения на встроенные/польз.
                        WordItem(
                            word          = word,
                            onEditClick   = { viewModel.onShowEditDialog(word) },
                            onDeleteClick = { viewModel.onDeleteWord(word.id) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    uiState.dialogState?.let { dialogState ->
        WordDialog(
            dialogState   = dialogState,
            onDismiss     = viewModel::onDialogDismiss,
            onTextChanged = viewModel::onDialogTextChanged,
            onConfirm     = viewModel::onDialogConfirm
        )
    }
}

@Composable
private fun WordItem(
    word: Word,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(word.word, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Edit,
                contentDescription = stringResource(R.string.sound_quiz_edit_word),
                modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Delete,
                contentDescription = stringResource(R.string.sound_quiz_delete_word),
                modifier = Modifier.size(18.dp),
                tint     = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun WordDialog(
    dialogState: WordDialogState,
    onDismiss: () -> Unit,
    onTextChanged: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (dialogState) {
        is WordDialogState.Add  -> stringResource(R.string.sound_quiz_dialog_new_word)
        is WordDialogState.Edit -> stringResource(R.string.sound_quiz_dialog_edit_word)
    }
    val text = when (dialogState) {
        is WordDialogState.Add  -> dialogState.text
        is WordDialogState.Edit -> dialogState.text
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(title) },
        text    = {
            Column {
                OutlinedTextField(
                    value         = text,
                    onValueChange = onTextChanged,
                    label         = { Text(stringResource(R.string.sound_quiz_dialog_word_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.sound_quiz_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.sound_quiz_cancel)) }
        }
    )
}
