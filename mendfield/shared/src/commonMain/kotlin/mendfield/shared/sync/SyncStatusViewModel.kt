// mendfield/shared/src/commonMain/kotlin/mendfield/shared/sync/SyncStatusViewModel.kt
package mendfield.shared.sync

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mendfield.core.data.SyncCoordinator
import mendfield.core.data.SyncStatus
import mendfield.core.sync.Operation
import mendfield.core.sync.RejectedOperation
import mendfield.core.sync.SyncState

enum class Tone { CALM, BUSY, WARNING }

@Immutable
data class RefusedRow(val operationId: String, val text: String)

@Immutable
data class SyncBarUiState(
    val text: String = "",
    val tone: Tone = Tone.CALM,
    val pending: Int = 0,
    val refused: List<RefusedRow> = emptyList(),
    val workingOffline: Boolean = false,
)

/** The strip at the bottom of every screen: what the outbox is doing. */
class SyncStatusViewModel(private val sync: SyncCoordinator) : ViewModel() {
    val state: StateFlow<SyncBarUiState> = sync.status.map(::syncBarState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncBarUiState())

    fun syncNow() {
        viewModelScope.launch { sync.syncNow() }
    }

    fun workOffline(enabled: Boolean) = sync.workOffline(enabled)

    fun dismiss(operationId: String) {
        viewModelScope.launch { sync.dismiss(operationId) }
    }
}

/** One sentence for the organizer; pure, so every case is tested. */
fun syncBarState(status: SyncStatus): SyncBarUiState {
    val n = status.pending
    val changes = if (n == 1) "1 change" else "$n changes"
    val state = status.state // a local value can be smart-cast
    val (text, tone) = when {
        status.workingOffline -> "Working offline · $changes waiting" to Tone.WARNING
        state is SyncState.Syncing -> "Sending $changes…" to Tone.BUSY
        state is SyncState.WaitingToRetry -> {
            val s = state.delay.inWholeMilliseconds / 1000.0
            "No answer, retrying in ${s.roundToInt()} s · $changes waiting" to Tone.WARNING
        }
        state is SyncState.Failed && n > 0 ->
            "Not sent: ${state.reason} · $changes waiting" to Tone.WARNING
        !status.online && n > 0 -> "Offline · $changes waiting" to Tone.WARNING
        !status.online -> "Offline · nothing to send" to Tone.WARNING
        n > 0 -> "$changes waiting" to Tone.BUSY
        else -> "All changes sent" to Tone.CALM
    }
    return SyncBarUiState(text, tone, n, status.rejected.map { it.toRow() },
        status.workingOffline)
}

private fun RejectedOperation.toRow(): RefusedRow {
    val what = when (val op = pending.operation) {
        is Operation.RegisterItem -> "Register \"${op.item.description}\""
        is Operation.AssignSlot -> "Put item ${op.itemId} in slot ${op.slotId}"
        is Operation.LogOutcome -> "Outcome for item ${op.outcome.itemId}"
    }
    return RefusedRow(pending.id, "$what: $message ($code)")
}
