@file:OptIn(ExperimentalCoroutinesApi::class)

package hearthseed.data.solutions

import hearthseed.data.cache.InMemoryCache
import hearthseed.data.domain.SowableVarieties
import hearthseed.data.fakes.FakeRemoteVarietySource
import hearthseed.data.repository.DataError
import hearthseed.data.repository.DataResult
import hearthseed.data.repository.VarietyRepository
import hearthseed.data.repository.getOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SolutionsTest {
    @Test
    fun ex1_everyErrorHasAMessage() {
        assertEquals(
            "The library server had a problem (503). Try again later.",
            DataError.Server(503).userMessage(),
        )
        assertEquals(
            "You are offline. Showing saved data if there is any.",
            DataError.Offline.userMessage(),
        )
    }

    @Test
    fun ex2_quickHarvestKeepsFastVarietiesFastestFirst() = runTest {
        val repository = VarietyRepository(FakeRemoteVarietySource())
        repository.refresh()
        val quick = QuickHarvest(SowableVarieties(repository))
        assertEquals(
            listOf("Larkmoor Winter Kale", "Commons Runner Bean"),
            quick(month = 4, maxDays = 70).first().map { it.commonName },
        )
        assertEquals(emptyList(), quick(month = 4, maxDays = 50).first())
    }

    @Test
    fun ex3_lotsAreCachedPerVarietyForOneMinute() = runTest {
        val time = TestTimeSource()
        val remote = FakeRemoteVarietySource()
        val lots = LotRepository(remote, InMemoryCache(1.minutes, time))
        val kale = lots.availableLots("v-kale-01")
        assertEquals(listOf("L-101"), kale.getOrNull()?.map { it.id })
        lots.availableLots("v-kale-01")
        assertEquals(1, remote.lotCalls)
        lots.availableLots("v-tom-07")
        assertEquals(2, remote.lotCalls)
        time += 61.seconds
        lots.availableLots("v-kale-01")
        assertEquals(3, remote.lotCalls)
        assertEquals(
            DataResult.Failure(DataError.NotFound),
            lots.availableLots("v-none"),
        )
    }

    @Test
    fun ex4_sameKeySharesOneLoadOtherKeysRunInParallel() = runTest {
        val cache = KeyedCache<String, String>(ttl = 5.minutes)
        val loads = mutableMapOf<String, Int>()
        suspend fun load(key: String): String {
            loads[key] = (loads[key] ?: 0) + 1
            delay(1.seconds)
            return key.uppercase()
        }
        val results = listOf("a", "a", "b").map { key ->
            async { cache.getOrLoad(key) { load(key) } }
        }.awaitAll()
        assertEquals(listOf("A", "A", "B"), results)
        assertEquals(mapOf("a" to 1, "b" to 1), loads)
        assertEquals(1_000, testScheduler.currentTime) // not 2 000
    }
}
