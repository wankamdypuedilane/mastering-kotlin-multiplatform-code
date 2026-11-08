package hearthseed.data

import hearthseed.data.cache.InMemoryCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class InMemoryCacheTest {
    private val time = TestTimeSource()
    private val cache =
        InMemoryCache<String, Int>(ttl = 5.minutes, timeSource = time)

    @Test
    fun entryIsFreshUntilTheTtlHasPassed() {
        cache.put("a", 1)
        time += 4.minutes + 59.seconds
        assertTrue(cache.peek("a")!!.isFresh)
        time += 1.seconds
        assertFalse(cache.peek("a")!!.isFresh)
        assertEquals(1, cache.peek("a")!!.value) // stale, but still there
    }

    @Test
    fun getOrLoadCallsLoadOnlyWhenMissingOrStale() = runTest {
        var loads = 0
        val load = suspend { ++loads }
        assertEquals(1, cache.getOrLoad("a", load = load))
        assertEquals(1, cache.getOrLoad("a", load = load))
        time += 5.minutes
        assertEquals(2, cache.getOrLoad("a", load = load))
        assertEquals(3, cache.getOrLoad("a", force = true, load = load))
    }

    @Test
    fun observeSeesTheLatestValue() = runTest {
        cache.put("a", 1)
        cache.put("a", 2)
        assertEquals(2, cache.observe("a").first())
        cache.invalidate("a")
        assertEquals(null, cache.observe("a").first())
    }
}
