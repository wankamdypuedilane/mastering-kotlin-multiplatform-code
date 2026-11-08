// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/SyncTransport.kt
package mendfield.core.sync

/** Sends one batch of operations to the server. */
fun interface SyncTransport {
    suspend fun push(batch: List<PendingOperation>): PushResult
}

sealed interface PushResult {
    /** The server answered; one result per operation it processed. */
    data class Delivered(val results: List<OperationResult>) : PushResult

    /** The batch may or may not have arrived (offline, timeout, 5xx). */
    data class Transient(val reason: String) : PushResult

    /** Resending will not help (a bad token, a malformed request). */
    data class Permanent(val reason: String) : PushResult
}

sealed interface OperationResult {
    val operationId: String

    data class Applied(override val operationId: String) : OperationResult

    data class Rejected(
        override val operationId: String,
        val code: String,
        val message: String,
    ) : OperationResult
}
