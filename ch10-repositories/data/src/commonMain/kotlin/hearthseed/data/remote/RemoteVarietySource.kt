package hearthseed.data.remote

/**
 * The network side of the catalog. Chapter 9 implements it with a Ktor client;
 * tests use a fake. Implementations throw [RemoteException] for transport and
 * HTTP failures, never Ktor or platform exception types.
 */
interface RemoteVarietySource {
    suspend fun varieties(): List<VarietyDto>
    suspend fun variety(id: String): VarietyDto
    suspend fun lots(varietyId: String): List<SeedLotDto>
}

/** The failures a remote source may report. */
sealed class RemoteException(message: String) : Exception(message) {
    /** No connection, DNS failure, timeout: the server was not reached. */
    class Unreachable(reason: String) : RemoteException(reason)

    /** The server answered with a non-success status. */
    class Http(val status: Int, val error: ErrorDto? = null) :
        RemoteException("HTTP $status")
}
