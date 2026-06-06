package ru.internet.boardgames.counter.presentation.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

/**
 * Проходит по цепочке контекстов вверх до нахождения [ComponentActivity].
 *
 * Необходимо вместо прямого каста `LocalContext.current as ComponentActivity`,
 * который падает с [ClassCastException] внутри ModalBottomSheet и боковых панелей,
 * потому что Material3 оборачивает контекст в [ContextWrapper] / ContextThemeWrapper.
 *
 * Использование:
 * ```kotlin
 * val activity = LocalContext.current.findActivity()
 * val vm: CounterViewModel = hiltViewModel(activity)
 * ```
 *
 * @throws IllegalStateException если [ComponentActivity] не найдена в цепочке контекстов.
 */
fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    error("ComponentActivity не найдена в цепочке контекстов: $this")
}
