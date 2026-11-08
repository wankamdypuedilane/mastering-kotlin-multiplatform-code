package hearthseed.data.solutions

import hearthseed.data.cache.InMemoryCache
import hearthseed.data.model.LotStatus
import hearthseed.data.model.SeedLot
import hearthseed.data.remote.RemoteVarietySource
import hearthseed.data.remote.toDomain
import hearthseed.data.repository.DataResult
import hearthseed.data.repository.dataCall
import hearthseed.data.repository.map
import kotlin.time.Duration.Companion.minutes

/** Exercise 10.3: lots per variety, cache-first, one minute of freshness. */
class LotRepository(
    private val remote: RemoteVarietySource,
    private val cache: InMemoryCache<String, List<SeedLot>> =
        InMemoryCache(ttl = 1.minutes),
) {
    /** The lots a member can borrow, best germination first. */
    suspend fun availableLots(varietyId: String): DataResult<List<SeedLot>> =
        dataCall {
            cache.getOrLoad(varietyId) {
                remote.lots(varietyId).map { it.toDomain() }
            }
        }.map { lots ->
            lots.filter { it.status == LotStatus.AVAILABLE }
                .sortedByDescending { it.germinationRate }
        }
}
