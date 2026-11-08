// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/Outbox.kt
package mendfield.core.sync

import kotlin.time.Clock
import kotlin.uuid.Uuid

/** Turns local changes into queued operations with fresh idempotency keys. */
class Outbox(
    private val store: OutboxStore,
    private val clock: Clock = Clock.System,
    private val newId: () -> String = { Uuid.random().toString() },
) {
    /** A fresh id for an item or outcome created on this device. */
    fun newEntityId(): String = newId()

    suspend fun enqueue(operation: Operation): PendingOperation {
        val pending = PendingOperation(newId(), clock.now(), operation)
        store.add(pending)
        return pending
    }
}
