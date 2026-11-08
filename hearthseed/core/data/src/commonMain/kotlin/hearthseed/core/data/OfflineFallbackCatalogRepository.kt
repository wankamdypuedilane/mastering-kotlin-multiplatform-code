// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/OfflineFallbackCatalogRepository.kt
package hearthseed.core.data

import hearthseed.core.data.local.VarietyLocalSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A decorator (Exercise 12.4, bound by the app's dataModule). When the server
 * cannot be reached, pages come from the varieties already seen and stored in
 * [local]; [servedFromDevice] tells the UI so. With Room (Chapter 14) that
 * copy survives a restart.
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
