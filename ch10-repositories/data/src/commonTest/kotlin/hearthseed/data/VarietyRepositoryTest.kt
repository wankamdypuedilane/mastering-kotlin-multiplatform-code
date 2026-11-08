@file:OptIn(ExperimentalCoroutinesApi::class)

package hearthseed.data

import hearthseed.data.cache.InMemoryCache
import hearthseed.data.fakes.FakeRemoteVarietySource
import hearthseed.data.fakes.SampleData
import hearthseed.data.model.Variety
import hearthseed.data.remote.RemoteException
import hearthseed.data.repository.DataError
import hearthseed.data.repository.DataResult
import hearthseed.data.repository.FetchPolicy
import hearthseed.data.repository.VarietyRepository
import hearthseed.data.repository.DataError.Offline
import hearthseed.data.repository.getOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class VarietyRepositoryTest {
    private val time = TestTimeSource()
    private val remote = FakeRemoteVarietySource()
    private val repository = VarietyRepository(
        remote = remote,
        cache = InMemoryCache(ttl = 5.minutes, timeSource = time),
    )

    private fun names(result: DataResult<List<Variety>>) =
        result.getOrNull()?.map { it.commonName }

    @Test
    fun cacheFirstCallsTheNetworkOncePerTtl() = runTest {
        repository.varieties()
        repository.varieties()
        assertEquals(1, remote.varietiesCalls)
        time += 5.minutes
        repository.varieties()
        assertEquals(2, remote.varietiesCalls)
    }

    @Test
    fun networkFirstAndForcedRefreshAlwaysCallTheNetwork() = runTest {
        repository.varieties(FetchPolicy.NETWORK_FIRST)
        repository.varieties(FetchPolicy.NETWORK_FIRST)
        repository.refresh(force = true)
        assertEquals(3, remote.varietiesCalls)
    }

    @Test
    fun offlineWithoutCacheIsAnError() = runTest {
        remote.failure = RemoteException.Unreachable("no route")
        assertEquals(DataResult.Failure(Offline), repository.varieties())
    }

    @Test
    fun offlineWithStaleCacheReturnsTheStaleData() = runTest {
        repository.refresh()
        time += 10.minutes
        remote.failure = RemoteException.Unreachable("no route")
        assertEquals(3, repository.varieties().getOrNull()?.size)
        val networkFirst = repository.varieties(FetchPolicy.NETWORK_FIRST)
        assertEquals(3, networkFirst.getOrNull()?.size)
        // refresh itself still reports the failure to the caller.
        assertEquals(DataResult.Failure(Offline), repository.refresh())
    }

    @Test
    fun observersSeeCachedDataThenTheRefreshedData() = runTest {
        repository.refresh()
        val seen = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVarieties().collect { list ->
                seen += list.map { it.id }
            }
        }
        remote.varieties = listOf(SampleData.bean)
        repository.refresh(force = true)
        remote.failure = RemoteException.Http(503)
        repository.refresh(force = true) // fails: observers keep the last data
        assertEquals(
            listOf(
                listOf("v-kale-01", "v-tom-07", "v-bean-03"),
                listOf("v-bean-03"),
            ),
            seen,
        )
    }

    @Test
    fun concurrentRefreshesShareOneNetworkCall() = runTest {
        remote.latency = 2.seconds
        launch { repository.refresh() }
        launch { repository.refresh() }
        advanceUntilIdle()
        assertEquals(1, remote.varietiesCalls)
        assertEquals(2_000, testScheduler.currentTime)
    }

    @Test
    fun cancellationIsNotTurnedIntoAFailure() = runTest {
        remote.latency = 2.seconds
        var result: DataResult<List<Variety>>? = null
        val job = launch { result = repository.refresh() }
        advanceTimeBy(1.seconds)
        job.cancel()
        advanceUntilIdle()
        assertNull(result)
        assertEquals(true, job.isCancelled)
    }

    @Test
    fun httpAndValidationErrorsAreMapped() = runTest {
        assertEquals(
            DataResult.Failure(DataError.NotFound),
            repository.variety("v-none"),
        )
        remote.varieties = listOf(SampleData.kale.copy(difficulty = "EXPERT"))
        assertIs<DataError.InvalidData>(
            (repository.variety("v-kale-01") as DataResult.Failure).error,
        )
        remote.failure = RemoteException.Http(503)
        assertEquals(
            DataResult.Failure(DataError.Server(503)),
            repository.variety("v-kale-01"),
        )
    }

    @Test
    fun invalidItemsAreDroppedFromTheCatalog() = runTest {
        val broken = SampleData.kale.copy(id = "v-x", sowingMonths = listOf(14))
        remote.varieties = SampleData.varieties + broken
        assertEquals(
            listOf(
                "Larkmoor Winter Kale",
                "Harbour Gold Tomato",
                "Commons Runner Bean",
            ),
            names(repository.varieties()),
        )
    }
}
