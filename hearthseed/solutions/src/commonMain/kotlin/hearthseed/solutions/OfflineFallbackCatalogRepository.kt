// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/OfflineFallbackCatalogRepository.kt
package hearthseed.solutions

import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.VarietyPage
import hearthseed.core.data.local.VarietyLocalSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Exercise 12.4: a decorator. When the server cannot be reached, pages come
 * from the varieties already seen; [servedFromDevice] tells the UI so.
 */
class OfflineFallbackCatalogRepository(
    private val delegate: CatalogRepository,
    private val local: VarietyLocalSource,
) : CatalogRepository by delegate {
    private val _servedFromDevice = MutableStateFlow(false)
    val servedFromDevice: StateFlow<Boolean> = _servedFromDevice.asStateFlow()

    override suspend fun varieties(
        query: String, family: String?, page: Int, pageSize: Int,
    ): DataResult<VarietyPage> {
        val remote = delegate.varieties(query, family, page, pageSize)
        val offline = remote is DataResult.Failure &&
            remote.error == DataError.Offline
        if (!offline) {
            if (remote is DataResult.Success) _servedFromDevice.value = false
            return remote // success, or an error the device cannot fix
        }
        val matches = local.search(query.trim(), family)
        if (matches.isEmpty() && page == 1) return remote // nothing seen yet
        _servedFromDevice.value = true
        val items = matches.drop((page - 1) * pageSize).take(pageSize)
        val result = VarietyPage(items, page, pageSize, matches.size)
        return DataResult.Success(result)
    }
}
