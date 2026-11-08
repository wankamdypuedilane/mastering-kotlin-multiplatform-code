package hearthseed.data.fakes

import hearthseed.data.remote.RemoteException
import hearthseed.data.remote.RemoteVarietySource
import hearthseed.data.remote.SeedLotDto
import hearthseed.data.remote.VarietyDto
import kotlin.time.Duration
import kotlinx.coroutines.delay

/**
 * An in-memory remote source whose data, latency and failures the test
 * controls. It counts calls so that tests can assert what the cache saved.
 */
class FakeRemoteVarietySource(
    var varieties: List<VarietyDto> = SampleData.varieties,
    var lots: Map<String, List<SeedLotDto>> = SampleData.lots,
) : RemoteVarietySource {
    var failure: RemoteException? = null   // thrown by every call when set
    var latency: Duration = Duration.ZERO  // virtual time under runTest
    var varietiesCalls = 0
        private set
    var lotCalls = 0
        private set

    private suspend fun respond() {
        delay(latency)
        failure?.let { throw it }
    }

    override suspend fun varieties(): List<VarietyDto> {
        varietiesCalls++
        respond()
        return varieties
    }

    override suspend fun variety(id: String): VarietyDto {
        respond()
        return varieties.find { it.id == id } ?: throw RemoteException.Http(404)
    }

    override suspend fun lots(varietyId: String): List<SeedLotDto> {
        lotCalls++
        respond()
        return lots[varietyId] ?: throw RemoteException.Http(404)
    }
}
