// ch04-germination-timer/timer/src/jvmMain/.../Demo.kt
package hearthseed.germination

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** Stands in for a blocking file read (JVM only). */
private fun readCalibrationFile(): Int {
    Thread.sleep(50)
    return 2
}

fun main() = runBlocking {        // JVM and Native only: blocks this thread
    val offset = withContext(Dispatchers.IO) { readCalibrationFile() }
    println("calibration offset: $offset")

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val tracker = GerminationTracker(appScope)
    val fast = 100.milliseconds
    val jobs = listOf(
        tracker.track(simulatedSensor("A", 10, listOf(3, 6, 9), fast)),
        tracker.track(simulatedSensor("B", 20, listOf(5, 8), fast)),
    )
    jobs.forEach { it.join() }
    println("rates: ${tracker.rates.value.toSortedMap()}")
    appScope.cancel()             // stops the stateIn sharing coroutine
}
