// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/bridge/FlowSubscription.kt
package hearthseed.shared.bridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** A running observation. Swift keeps it and calls close() to stop it. */
class FlowSubscription internal constructor(private val job: Job) {
    val isActive: Boolean get() = job.isActive
    fun close() = job.cancel()
}

/** Collects [flow] in this scope and hands every value to [onEach]. */
internal fun <T> CoroutineScope.subscribe(
    flow: Flow<T>,
    onEach: (T) -> Unit,
): FlowSubscription = FlowSubscription(launch { flow.collect { onEach(it) } })

/**
 * Owns one ViewModel outside Compose: SwiftUI has no ViewModelStore, so the
 * bridge keeps its own and clears it in close().
 */
internal class ViewModelHolder<VM : ViewModel>(
    type: KClass<VM>,
    create: () -> VM,
) {
    private val store = ViewModelStore()
    val viewModel: VM = ViewModelProvider.create(
        store, viewModelFactory { addInitializer(type) { create() } },
    )[type]

    fun clear() = store.clear() // onCleared(), cancels viewModelScope
}
