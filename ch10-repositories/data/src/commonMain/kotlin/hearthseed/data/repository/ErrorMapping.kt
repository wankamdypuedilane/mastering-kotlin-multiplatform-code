package hearthseed.data.repository

import hearthseed.data.remote.InvalidDataException
import hearthseed.data.remote.RemoteException

/** Translates the data-layer exceptions into [DataError]. */
fun RemoteException.toDataError(): DataError = when (this) {
    is RemoteException.Unreachable -> DataError.Offline
    is RemoteException.Http ->
        if (status == 404) DataError.NotFound else DataError.Server(status)
}

/**
 * Runs [block] and converts the known failures into [DataResult.Failure].
 * It catches only the two exception types of the data layer, so a
 * CancellationException (and any programming error) still propagates.
 */
suspend fun <T> dataCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: RemoteException) {
        DataResult.Failure(e.toDataError())
    } catch (e: InvalidDataException) {
        DataResult.Failure(DataError.InvalidData(e.message ?: "invalid data"))
    }
