package ru.internet.boardgames.spygame.presentation.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.spygame.R
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/**
 * Кнопка «Новая игра» — запускает сессию с другой категорией.
 *
 * @param onClick   Вызывает GameViewModel.refreshGame().
 * @param isLoading Пока true — кнопка заблокирована, иконка непрерывно вращается.
 * @param expanded  true → [ExtendedFloatingActionButton] с текстом,
 *                  false → компактный [FloatingActionButton].
 */
@Composable
fun RefreshButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    expanded: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RefreshIconRotation")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RefreshIconRotationValue"
    )

    val iconRotation = if (isLoading) spinRotation else 0f

    val iconModifier = Modifier
        .size(24.dp)
        .graphicsLayer { rotationZ = iconRotation }

    if (expanded) {
        ExtendedFloatingActionButton(
            onClick        = { if (!isLoading) onClick() },
            modifier       = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
            icon           = {
                Icon(
                    imageVector        = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.refresh_game_desc),
                    modifier           = iconModifier
                )
            },
            text = {
                Text(
                    text  = stringResource(R.string.refresh_game_label),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )
    } else {
        FloatingActionButton(
            onClick        = { if (!isLoading) onClick() },
            modifier       = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector        = Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.refresh_game_desc),
                modifier           = iconModifier
            )
        }
    }
}

@Preview
@Composable
private fun RefreshButtonPreview() {
    SpyGameTheme { RefreshButton(onClick = {}, isLoading = false) }
}

@Preview
@Composable
private fun RefreshButtonLoadingPreview() {
    SpyGameTheme { RefreshButton(onClick = {}, isLoading = true) }
}
