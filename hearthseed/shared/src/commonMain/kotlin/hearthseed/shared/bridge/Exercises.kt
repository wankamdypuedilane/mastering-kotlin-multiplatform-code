// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/bridge/Exercises.kt
package hearthseed.shared.bridge

import androidx.lifecycle.viewModelScope
import hearthseed.core.data.userMessage
import hearthseed.core.model.Loan
import hearthseed.feature.loans.MyLoansUiState
import hearthseed.feature.loans.MyLoansViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Solutions of the Chapter 16 exercises (Kotlin side).

/** Exercise 16.2: the footer line of the SwiftUI catalog. */
fun CatalogSnapshot.countLabel(): String = when {
    phase != CatalogPhase.LOADED -> ""
    varieties.size == total -> "$total varieties"
    else -> "${varieties.size} of $total varieties"
}

/** Exercise 16.3: "My loans" as Swift reads it. */
data class MyLoansSnapshot(
    val isLoading: Boolean,
    val open: List<Loan>,
    val returned: List<Loan>,
    val message: String?,
)

internal fun MyLoansUiState.toSnapshot(): MyLoansSnapshot = when (this) {
    MyLoansUiState.Loading -> MyLoansSnapshot(true, none, none, null)
    MyLoansUiState.Empty -> MyLoansSnapshot(false, none, none, null)
    is MyLoansUiState.Error ->
        MyLoansSnapshot(false, none, none, error.userMessage())
    is MyLoansUiState.Loans -> MyLoansSnapshot(false, open, returned, null)
}

private val none = emptyList<Loan>()

/** Exercise 16.3: the loans screen for SwiftUI. */
class MyLoansObserver internal constructor(create: () -> MyLoansViewModel) {
    private val holder = ViewModelHolder(MyLoansViewModel::class, create)
    private val viewModel get() = holder.viewModel

    fun observe(onChange: (MyLoansSnapshot) -> Unit): FlowSubscription =
        viewModel.viewModelScope.subscribe(
            viewModel.state.map { it.toSnapshot() }, onChange,
        )

    fun refresh() = viewModel.refresh()
    fun close() = holder.clear()
}

/**
 * Exercise 16.4: any flow, for Swift. Generic classes keep their type
 * parameter in the header; `T : Any` makes the values non-optional.
 */
class FlowWatcher<T : Any> internal constructor(
    private val flow: Flow<T>,
    private val scope: CoroutineScope,
) {
    fun watch(onEach: (T) -> Unit): FlowSubscription =
        scope.subscribe(flow, onEach)
}

/** Exercise 16.4: every catalog snapshot, through the generic watcher. */
fun CatalogObserver.snapshots(): FlowWatcher<CatalogSnapshot> = FlowWatcher(
    viewModel.state.map { it.toSnapshot() }, viewModel.viewModelScope,
)
