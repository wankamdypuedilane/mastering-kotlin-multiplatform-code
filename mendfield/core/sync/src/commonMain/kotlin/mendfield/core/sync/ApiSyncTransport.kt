// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/ApiSyncTransport.kt
package mendfield.core.sync

import mendfield.core.network.ApiResult
import mendfield.core.network.MendfieldApi
import mendfield.core.network.SyncStatus

/** The real transport: POST /api/v1/sync through [MendfieldApi]. */
class ApiSyncTransport(private val api: MendfieldApi) : SyncTransport {
    override suspend fun push(batch: List<PendingOperation>): PushResult =
        when (val result = api.sync(batch.map { it.toDto() })) {
            is ApiResult.Success -> PushResult.Delivered(
                result.value.map { dto ->
                    when (dto.status) {
                        SyncStatus.APPLIED ->
                            OperationResult.Applied(dto.operationId)
                        SyncStatus.REJECTED -> OperationResult.Rejected(
                            dto.operationId,
                            dto.code ?: "rejected",
                            dto.message ?: "Rejected by the server",
                        )
                    }
                },
            )
            is ApiResult.Failure ->
                if (result.error.isTransient) {
                    PushResult.Transient(result.error.toString())
                } else {
                    PushResult.Permanent(result.error.toString())
                }
        }
}
