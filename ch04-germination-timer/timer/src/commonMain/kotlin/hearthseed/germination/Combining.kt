// ch04-germination-timer/timer/src/commonMain/.../Combining.kt
package hearthseed.germination

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.zip

/** Mean rate of two trays, recomputed whenever either one reports. */
fun meanRate(a: Flow<GerminationReading>, b: Flow<GerminationReading>) =
    combine(a, b) { x, y -> (x.ratePercent + y.ratePercent) / 2 }

/** Pairs the n-th photo of each tray and names the tray that is ahead. */
fun leaderPerPhoto(a: Flow<GerminationReading>, b: Flow<GerminationReading>) =
    a.zip(b) { x, y ->
        if (x.ratePercent >= y.ratePercent) x.trayId else y.trayId
    }

/** Every reading of every tray in one stream, in arrival order. */
fun allReadings(vararg trays: Flow<GerminationReading>) = merge(*trays)

/** Follows the selected tray; a new selection cancels the old sensor. */
@OptIn(ExperimentalCoroutinesApi::class)       // flatMapLatest, in 1.11.0
fun Flow<String>.followTray(sensorFor: (String) -> Flow<GerminationReading>) =
    flatMapLatest { trayId -> sensorFor(trayId) }
