package ru.internet.boardgames.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ─── Константы ────────────────────────────────────────────────────────────────

/** Ширина панели относительно ширины экрана. */
private const val PANEL_WIDTH = 0.88f

/** Максимальное затемнение скрима. */
private const val SCRIM_ALPHA = 0.5f

/**
 * Доля ширины панели, которую нужно сдвинуть вправо, чтобы закрыть.
 * 30% → небольшое движение уже закрывает, если скорость достаточна.
 */
private const val CLOSE_SWIPE_THRESHOLD = 0.30f

/**
 * Доля ширины панели, которую нужно «вытянуть» влево из правого края,
 * чтобы панель открылась при отпускании.
 */
private const val OPEN_SWIPE_THRESHOLD = 0.40f

/** Ширина невидимой зоны у правого края для edge-свайпа. */
private val EDGE_TRIGGER_WIDTH = 24.dp

private const val OPEN_DURATION_MS  = 280
private const val CLOSE_DURATION_MS = 220

/**
 * Боковая панель, выезжающая справа поверх текущего экрана.
 *
 * Анимация полностью следует за пальцем (как Navigation Drawer):
 * - Edge-свайп влево   → панель «тянется» за пальцем, при отпускании
 *                         доводится до конца или возвращается.
 * - Смена направления  → панель следует обратно (плавная отмена открытия).
 * - Свайп вправо       → панель «едет» за пальцем, закрывается при пороге.
 * - Скрим              → прозрачность синхронизирована с позицией панели.
 * - Back-жест          → закрывает панель; BackHandler зарегистрирован внутри
 *                         компонента, что гарантирует наивысший LIFO-приоритет
 *                         перед BackHandler'ами экранов внутри NavHost.
 *
 * System bars: панель и зона edge-свайпа уважают [WindowInsets.systemBars]
 * (статус-бар сверху, навигационный бар снизу). Скрим намеренно остаётся
 * полноэкранным — стандартное поведение Material drawer.
 *
 * @param visible          Показать / скрыть панель программно.
 * @param onDismiss        Вызывается при закрытии (тап скрим / свайп вправо / Back).
 * @param onOpen           Вызывается при edge-свайпе (устанавливает visible=true снаружи).
 * @param edgeSwipeEnabled false — зона edge-свайпа не рендерится.
 *                         Передайте false на HOME_ROUTE и экранах Counter.
 * @param content          Содержимое панели.
 */
@Composable
fun CounterSidePanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit = {},
    edgeSwipeEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // ── Back-жест ─────────────────────────────────────────────────────────────
    // BackHandler размещён здесь, а не в NavGraph. Это гарантирует, что он
    // регистрируется в OnBackPressedDispatcher ПОСЛЕ всего содержимого NavHost
    // (LIFO), поэтому перехватывает Back раньше любых BackHandler'ов экранов.
    BackHandler(enabled = visible) {
        onDismiss()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // panelWidthPx — ширина панели в пикселях (constraints.maxWidth уже в px)
        val panelWidthPx = constraints.maxWidth.toFloat() * PANEL_WIDTH

        // Вертикальные insets системных баров: статус-бар (top) и навигация (bottom).
        // Вычисляем один раз здесь, чтобы не повторять в каждом дочернем composable.
        // asPaddingValues() преобразует WindowInsets в Dp, безопасные для Modifier.padding.
        val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
        val systemBarsTop    = systemBarsPadding.calculateTopPadding()
        val systemBarsBottom = systemBarsPadding.calculateBottomPadding()

        // Единственный источник истины о положении панели.
        // 0f = полностью открыта, panelWidthPx = полностью закрыта (за правым краем).
        // Инициализация: закрытое состояние.
        val panelOffset = remember { Animatable(panelWidthPx) }

        // Синхронизация с внешним visible.
        // Если drag уже довёл панель к цели, animateTo завершится мгновенно.
        LaunchedEffect(visible, panelWidthPx) {
            panelOffset.animateTo(
                targetValue   = if (visible) 0f else panelWidthPx,
                animationSpec = if (visible)
                    tween(OPEN_DURATION_MS, easing = FastOutSlowInEasing)
                else
                    tween(CLOSE_DURATION_MS, easing = FastOutLinearInEasing)
            )
        }

        // ── Скрим ──────────────────────────────────────────────────────────────
        // Рендерится только пока панель не в полностью закрытом положении.
        // derivedStateOf: пересчитывает only when crossing threshold → минимум recomposition.
        val panelIsMoving by remember { derivedStateOf { panelOffset.value < panelWidthPx - 1f } }

        if (panelIsMoving) {
            Box(
                Modifier
                    .fillMaxSize()
                    // graphicsLayer читает panelOffset в draw-phase (без recomposition)
                    .graphicsLayer {
                        alpha = ((panelWidthPx - panelOffset.value) / panelWidthPx * SCRIM_ALPHA)
                            .coerceIn(0f, SCRIM_ALPHA)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { if (visible) onDismiss() }
            )
        }

        // ── Панель ─────────────────────────────────────────────────────────────
        // graphicsLayer { translationX } — draw-phase, не вызывает re-layout.
        // pointerInput(visible): при visible=false возвращается сразу →
        //   события проходят сквозь панель к контенту ниже (NavHost, edge-trigger).
        Surface(
            modifier      = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(PANEL_WIDTH)
                // graphicsLayer — draw-phase: не вызывает re-layout.
                // Размещён ДО padding: при трансляции весь слой (включая отступы)
                // движется единым блоком — панель выезжает как единое целое.
                .graphicsLayer { translationX = panelOffset.value }
                // Вертикальные insets: панель не заходит под статус-бар и навигацию.
                // Порядок важен: padding идёт ПОСЛЕ graphicsLayer (входит в слой,
                // который транслируется) и ДО pointerInput (сужает зону касаний).
                .padding(top = systemBarsTop, bottom = systemBarsBottom)
                // Жест закрытия: свайп вправо по открытой панели
                .pointerInput(visible) {
                    if (!visible) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (panelOffset.value > panelWidthPx * CLOSE_SWIPE_THRESHOLD) {
                                    // Порог пройден → доводим вправо и закрываем
                                    panelOffset.animateTo(
                                        panelWidthPx,
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                    onDismiss()
                                } else {
                                    // Не добрали → возвращаем на место
                                    panelOffset.animateTo(
                                        0f,
                                        spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Правый свайп закрывает; левый не уходит за 0f
                            if (dragAmount > 0 || panelOffset.value > 0f) {
                                coroutineScope.launch {
                                    panelOffset.snapTo(
                                        (panelOffset.value + dragAmount).coerceIn(0f, panelWidthPx)
                                    )
                                }
                            }
                        }
                    )
                },
            shadowElevation = 16.dp,
            tonalElevation  = 1.dp,
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
        ) {
            content()
        }

        // ── Edge-свайп (только когда закрыта и разрешён) ─────────────────────
        // Невидимая полоска 24dp у правого края.
        // Рендерится поверх панели (последний child → выше в Z-order).
        // Жест в обоих направлениях следует за пальцем:
        //   влево  → тянет панель к открытому состоянию,
        //   вправо → возвращает панель обратно (отмена открытия).
        if (!visible && edgeSwipeEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    // Edge-зона активна только между системными барами —
                    // симметрично границам панели.
                    .padding(top = systemBarsTop, bottom = systemBarsBottom)
                    .width(EDGE_TRIGGER_WIDTH)
                    .pointerInput(panelWidthPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    // Если вытянули более OPEN_SWIPE_THRESHOLD → открываем
                                    val openThreshold = panelWidthPx * (1f - OPEN_SWIPE_THRESHOLD)
                                    if (panelOffset.value < openThreshold) {
                                        panelOffset.animateTo(
                                            0f,
                                            spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                        onOpen() // visible станет true снаружи
                                    } else {
                                        // Не хватило (или смена направления) → прячем обратно
                                        panelOffset.animateTo(
                                            panelWidthPx,
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                // Оба направления: панель следует за пальцем в реальном времени.
                                // coerceIn гарантирует нахождение в допустимом диапазоне [0, panelWidthPx].
                                coroutineScope.launch {
                                    panelOffset.snapTo(
                                        (panelOffset.value + dragAmount).coerceIn(0f, panelWidthPx)
                                    )
                                }
                            }
                        )
                    }
            )
        }
    }
}
