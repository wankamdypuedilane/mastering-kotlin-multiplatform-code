// ch04-germination-timer/solutions/src/commonMain/.../Solutions.kt
package hearthseed.germination.solutions

import hearthseed.germination.GerminationReading
import hearthseed.germination.TrayRead
import kotlin.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withTimeoutOrNull

// Exercise 4.1: stop collecting once every seed has sprouted.
fun Flow<GerminationReading>.untilFullyGerminated(): Flow<GerminationReading> =
    transformWhile { reading ->
        emit(reading)
        reading.sprouted < reading.sown   // false ends the flow
    }

// Exercise 4.2: read all trays in parallel, each with its own time limit.
suspend fun readAllWithin(
    limit: Duration,
    sensors: Map<String, TrayRead>,
): Map<String, Int?> = coroutineScope {
    val pending = sensors.mapValues { (_, read) ->
        async { withTimeoutOrNull(limit) { read() } }
    }
    pending.mapValues { (_, deferred) -> deferred.await() }
}

// Exercise 4.3: emit the tray id when no reading arrives within [window].
@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<GerminationReading>.stallAlerts(window: Duration): Flow<String> =
    transformLatest { reading ->
        delay(window)            // cancelled if a newer reading arrives
        emit(reading.trayId)
    }
