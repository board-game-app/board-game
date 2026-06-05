package ru.internet.boardgames.spygame.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.internet.boardgames.spygame.presentation.theme.SpyGameTheme

/**
 * Рубашка карточки — лицевая сторона до переворота.
 *
 * Показывает: название категории + порядковый номер карточки.
 * Пользователь видит эту сторону, пока не нажмёт на карточку.
 *
 * @param categoryName  Локализованное название категории ("Аэропорт", "Airport").
 * @param cardNumber    Порядковый номер карточки (1-based), отображается как "№ N".
 */
@Composable
fun CardFront(
    categoryName: String,
    cardNumber: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 0.dp  // Shadow управляется снаружи (SpyCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка-иллюстрация — подчёркивает тему «слежки»
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Название категории — то, что все игроки знают (кроме шпиона)
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Номер карточки
            Text(
                text = "№ $cardNumber",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardFrontPreview() {
    SpyGameTheme {
        CardFront(
            categoryName = "Аэропорт",
            cardNumber = 3,
            modifier = Modifier.size(220.dp, 308.dp)
        )
    }
}
