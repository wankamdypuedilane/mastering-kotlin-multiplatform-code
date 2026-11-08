package hearthseed.data.cache

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** A cached value and whether it is younger than the time-to-live. */
data class Cached<out V>(val value: V, val isFresh: Boolean)

/**
 * A memory cache with a time-to-live per entry. Ages are measured with a
 * [TimeSource] (monotonic by default, unlike the wall clock), so setting the
 * device clock back does not make an entry fresh again; tests pass a
 * TestTimeSource.
 */
class InMemoryCache<K, V : Any>(
    private val ttl: Duration,
    private val timeSource: TimeSource.WithComparableMarks =
        TimeSource.Monotonic,
) {
    private class Entry<V>(val value: V, val storedAt: ComparableTimeMark)

    private val entries = MutableStateFlow<Map<K, Entry<V>>>(emptyMap())
    private val mutex = Mutex()

    private fun Entry<V>.isFresh() = storedAt.elapsedNow() < ttl

    /** The current entry, fresh or stale, without loading anything. */
    fun peek(key: K): Cached<V>? =
        entries.value[key]?.let { Cached(it.value, it.isFresh()) }

    /** Emits the value for [key] now and after every change (null = none). */
    fun observe(key: K): Flow<V?> =
        entries.map { it[key]?.value }.distinctUntilChanged()

    fun put(key: K, value: V) {
        entries.update { it + (key to Entry(value, timeSource.markNow())) }
    }

    fun invalidate(key: K) {
        entries.update { it - key }
    }

    /**
     * Returns a fresh value, calling [load] only when the entry is missing,
     * stale or [force] is true. The mutex makes concurrent callers wait for
     * the first load instead of starting their own (a "single flight").
     */
    suspend fun getOrLoad(
        key: K,
        force: Boolean = false,
        load: suspend () -> V,
    ): V = mutex.withLock {
        val current = entries.value[key]
        if (!force && current != null && current.isFresh()) {
            current.value
        } else {
            load().also { put(key, it) }
        }
    }
}
