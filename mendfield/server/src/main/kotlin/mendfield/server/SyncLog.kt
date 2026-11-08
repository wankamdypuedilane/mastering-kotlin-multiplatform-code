// mendfield/server/src/main/kotlin/mendfield/server/SyncLog.kt
package mendfield.server

/**
 * The idempotency log of POST /api/v1/sync. Each operation id is answered
 * once: a replay of the same operation gets the stored result, APPLIED or
 * REJECTED, and changes nothing. The same id with another operation is
 * refused and not stored. Callers hold the store's lock.
 */
internal class SyncLog {
    private data class Entry(
        val operation: SyncOperationDto,
        val result: SyncResultDto,
    )

    private val entries = HashMap<String, Entry>()

    fun answer(op: SyncOperationDto, apply: () -> Unit): SyncResultDto {
        val stored = entries[op.id]
        return when {
            stored == null ->
                run(op, apply).also { entries[op.id] = Entry(op, it) }
            stored.operation == op -> stored.result
            else -> rejected(op, "operation_id_reused",
                "Operation ${op.id} was already used for another change")
        }
    }

    // A rule violation is a result, not an HTTP error. Any other exception
    // propagates (a 500): nothing is stored, so the client's retry is safe.
    private fun run(op: SyncOperationDto, apply: () -> Unit): SyncResultDto =
        try {
            apply()
            SyncResultDto(op.id, SyncStatus.APPLIED)
        } catch (e: ApiException) {
            rejected(op, e.code, e.message)
        }

    private fun rejected(op: SyncOperationDto, code: String, message: String) =
        SyncResultDto(op.id, SyncStatus.REJECTED, code, message)
}
