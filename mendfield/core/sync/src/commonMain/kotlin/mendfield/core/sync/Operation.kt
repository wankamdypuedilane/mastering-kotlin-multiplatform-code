// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/Operation.kt
package mendfield.core.sync

import kotlin.time.Instant
import mendfield.core.model.Item
import mendfield.core.model.Outcome

/**
 * A change made on the device that the server has not confirmed yet.
 * Entities carry client-generated ids, so an item registered offline keeps
 * its id on the server.
 */
sealed interface Operation {
    data class RegisterItem(val item: Item) : Operation
    data class AssignSlot(val slotId: String, val itemId: String) : Operation
    data class LogOutcome(val outcome: Outcome) : Operation
}

/** An operation in the outbox. [id] is its idempotency key on the server. */
data class PendingOperation(
    val id: String,
    val createdAt: Instant,
    val operation: Operation,
)

/** An operation the server refused for good, kept for the organizer to see. */
data class RejectedOperation(
    val pending: PendingOperation,
    val code: String,
    val message: String,
)
