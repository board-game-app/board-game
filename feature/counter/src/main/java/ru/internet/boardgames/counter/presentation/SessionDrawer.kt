package ru.internet.boardgames.counter.presentation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.counter.domain.model.Session

/**
 * Боковой drawer управления сессиями (Изменение 4).
 *
 * Содержимое:
 *   • Заголовок «Сессии» + кнопка [＋] для создания новой сессии.
 *   • LazyColumn со списком сессий:
 *       – Активная сессия отмечена галочкой [Icons.Default.Check] слева.
 *       – Тап на строку → переключить активную сессию + закрыть drawer.
 *       – Долгий тап → inline-переименование (OutlinedTextField с ✓ / ✕).
 *       – Иконка [🗑] → диалог подтверждения удаления.
 *
 * @param sessions           Список всех сессий.
 * @param activeSessionId    Id активной сессии (null если не выбрана).
 * @param renamingSessionId  Id сессии в режиме переименования; null = нет режима.
 * @param renamingSessionName Текущий текст в поле переименования.
 * @param onSessionClick     Тап на сессию — переключить и закрыть drawer.
 * @param onStartRename      Долгий тап — начать inline-переименование.
 * @param onRenameNameChange Изменение текста в поле переименования.
 * @param onConfirmRename    Кнопка ✓ — сохранить новое имя.
 * @param onCancelRename     Кнопка ✕ — отменить переименование.
 * @param onDeleteRequest    Иконка 🗑 — запросить подтверждение удаления.
 * @param onCreateSession    Кнопка ＋ — открыть CreateSessionDialog.
 */
@Composable
internal fun SessionDrawer(
    sessions: List<Session>,
    activeSessionId: Long?,
    renamingSessionId: Long?,
    renamingSessionName: String,
    onSessionClick: (Long) -> Unit,
    onStartRename: (Session) -> Unit,
    onRenameNameChange: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onCancelRename: () -> Unit,
    onDeleteRequest: (Session) -> Unit,
    onCreateSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        // ── Заголовок ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сессии",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            // Кнопка «＋ Создать новую сессию»
            IconButton(onClick = onCreateSession) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать сессию"
                )
            }
        }

        HorizontalDivider()

        // ── Список сессий ─────────────────────────────────────────────────────
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = sessions, key = { it.id }) { session ->
                val isActive = session.id == activeSessionId
                val isRenaming = session.id == renamingSessionId

                if (isRenaming) {
                    // ── Режим inline-переименования ───────────────────────────
                    SessionRenameRow(
                        value = renamingSessionName,
                        onValueChange = onRenameNameChange,
                        onConfirm = onConfirmRename,
                        onCancel = onCancelRename,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                } else {
                    // ── Обычная строка сессии ─────────────────────────────────
                    SessionRow(
                        session = session,
                        isActive = isActive,
                        onSessionClick = { onSessionClick(session.id) },
                        onLongClick = { onStartRename(session) },
                        onDeleteRequest = { onDeleteRequest(session) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ── Строка сессии ─────────────────────────────────────────────────────────────

@Composable
private fun SessionRow(
    session: Session,
    isActive: Boolean,
    onSessionClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .combinedClickable(
                onClick = onSessionClick,
                onLongClick = onLongClick
            )
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Галочка для активной сессии
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Активная сессия",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Название сессии
        Text(
            text = session.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Иконка удаления
        IconButton(onClick = onDeleteRequest) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить сессию «${session.name}»",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ── Строка inline-переименования ─────────────────────────────────────────────

@Composable
private fun SessionRenameRow(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Автофокус на поле при появлении
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )
        // Кнопка «✓» — сохранить
        IconButton(
            onClick = onConfirm,
            enabled = value.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Сохранить",
                tint = if (value.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        // Кнопка «✕» — отменить
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Отменить переименование",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
