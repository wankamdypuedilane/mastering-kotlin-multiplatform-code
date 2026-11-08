// ch04-germination-timer/timer/src/commonMain/.../GerminationTracker.kt
package hearthseed.germination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A one-off event: a tray crossed the ready threshold. */
data class TrayReady(val trayId: String, val ratePercent: Int)

/** Collects tray sensors and exposes the germination rate per tray. */
class GerminationTracker(
    private val scope: CoroutineScope,
    private val readyAt: Int = 80,
) {
    private val _rates = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Latest germination rate per tray, in percent. */
    val rates: StateFlow<Map<String, Int>> = _rates.asStateFlow()

    private val _ready = MutableSharedFlow<TrayReady>()
    /** Hot stream of events; subscribers miss events sent before they join. */
    val ready: SharedFlow<TrayReady> = _ready.asSharedFlow()

    /** Average over all trays, computed only while someone observes it. */
    val averageRate: StateFlow<Int> = rates
        .map { if (it.isEmpty()) 0 else it.values.sum() / it.size }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Collects one sensor into the state; returns the collecting job. */
    fun track(sensor: Flow<GerminationReading>): Job = scope.launch {
        sensor.collect { reading ->
            val before = _rates.value[reading.trayId] ?: 0
            _rates.update { it + (reading.trayId to reading.ratePercent) }
            if (before < readyAt && reading.ratePercent >= readyAt) {
                _ready.emit(TrayReady(reading.trayId, reading.ratePercent))
            }
        }
    }
}
