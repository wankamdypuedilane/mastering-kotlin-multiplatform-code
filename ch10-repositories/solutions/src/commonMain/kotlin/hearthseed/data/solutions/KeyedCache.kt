package hearthseed.data.solutions

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Exercise 10.4: a single flight per key. Loads of the same key share one
 * call; loads of different keys run in parallel.
 */
class KeyedCache<K, V : Any>(
    private val ttl: Duration,
    private val timeSource: TimeSource.WithComparableMarks =
        TimeSource.Monotonic,
) {
    private class Slot<V : Any> {
        val mutex = Mutex()
        var value: V? = null
        var storedAt: ComparableTimeMark? = null
    }

    private val guard = Mutex() // protects the map, held only briefly
    private val slots = mutableMapOf<K, Slot<V>>()

    suspend fun getOrLoad(
        key: K,
        force: Boolean = false,
        load: suspend () -> V,
    ): V {
        val slot = guard.withLock { slots.getOrPut(key) { Slot() } }
        return slot.mutex.withLock {
            val value = slot.value
            val age = slot.storedAt?.elapsedNow()
            if (!force && value != null && age != null && age < ttl) {
                value
            } else {
                load().also {
                    slot.value = it
                    slot.storedAt = timeSource.markNow()
                }
            }
        }
    }
}
