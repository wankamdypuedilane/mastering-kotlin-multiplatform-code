package hearthseed.data.repository

import hearthseed.data.cache.InMemoryCache
import hearthseed.data.model.Variety
import hearthseed.data.remote.RemoteVarietySource
import hearthseed.data.remote.toDomain
import hearthseed.data.remote.toDomainSkippingInvalid
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

/**
 * The single source of truth for the catalog: callers observe the cache,
 * and only [refresh] writes to it from the network.
 */
class VarietyRepository(
    private val remote: RemoteVarietySource,
    private val cache: InMemoryCache<String, List<Variety>> =
        InMemoryCache(ttl = 5.minutes),
) {
    /** The catalog now and after every successful refresh. */
    fun observeVarieties(): Flow<List<Variety>> =
        cache.observe(CATALOG).filterNotNull()

    /** Loads the catalog unless the cache is fresh; [force] skips the check. */
    suspend fun refresh(force: Boolean = false): DataResult<List<Variety>> =
        dataCall {
            cache.getOrLoad(CATALOG, force) {
                remote.varieties().toDomainSkippingInvalid()
            }
        }

    /** A one-shot read; on a network failure, stale data beats an error. */
    suspend fun varieties(
        policy: FetchPolicy = FetchPolicy.CACHE_FIRST,
    ): DataResult<List<Variety>> {
        val result = refresh(force = policy == FetchPolicy.NETWORK_FIRST)
        val stale = cache.peek(CATALOG)
        return if (result is DataResult.Failure && stale != null) {
            DataResult.Success(stale.value)
        } else {
            result
        }
    }

    /** One variety, straight from the network (the detail screen). */
    suspend fun variety(id: String): DataResult<Variety> =
        dataCall { remote.variety(id).toDomain() }

    private companion object { const val CATALOG = "catalog" }
}
