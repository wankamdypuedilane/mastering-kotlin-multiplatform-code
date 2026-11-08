// ch04-germination-timer/timer/src/commonMain/.../Sensor.kt
package hearthseed.germination

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * A simulated tray sensor. The flow is cold: nothing runs until a
 * collector arrives, and every collector gets its own run.
 */
fun simulatedSensor(
    trayId: String,
    sown: Int,
    sproutCounts: List<Int>,
    interval: Duration = 1.hours,
): Flow<GerminationReading> = flow {
    for (count in sproutCounts) {
        delay(interval)                       // wait for the next photo
        emit(GerminationReading(trayId, sown, count))
    }
}

/** Emits each new multiple of ten percent reached: 10, 20, 40... */
fun Flow<GerminationReading>.milestones(): Flow<Int> =
    map { it.ratePercent / 10 * 10 }
        .distinctUntilChanged()
        .filter { it > 0 }
