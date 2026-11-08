// ch04-germination-timer/timer/src/commonMain/.../Failures.kt
package hearthseed.germination

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope

typealias TrayRead = suspend () -> Int

/** All or nothing: the first failing tray cancels the others. */
suspend fun readAllTraysStrict(sensors: Map<String, TrayRead>): List<Int> =
    coroutineScope {
        sensors.values.map { read -> async { read() } }.awaitAll()
    }

/** Best effort: a failing tray does not cancel the others. */
suspend fun readAllTrays(
    sensors: Map<String, TrayRead>,
): Map<String, Result<Int>> = supervisorScope {
    val pending = sensors.mapValues { (_, read) -> async { read() } }
    pending.mapValues { (_, deferred) ->
        try {
            Result.success(deferred.await())
        } catch (e: CancellationException) {
            throw e           // never swallow cancellation
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
