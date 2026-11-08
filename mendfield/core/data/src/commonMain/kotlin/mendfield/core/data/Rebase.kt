// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/Rebase.kt
package mendfield.core.data

import mendfield.core.sync.Operation

/**
 * The server's copy of an event with the device's unsent [operations]
 * applied on top, in queue order. A refresh stores this, never the bare
 * server copy: otherwise an item registered offline would vanish from the
 * screen until the outbox is delivered. Operations of other events, and
 * assignments to slots the server no longer has, change nothing.
 */
fun EventData.withPending(
    eventId: String,
    operations: List<Operation>,
): EventData = operations.fold(this) { data, op -> data.apply(eventId, op) }

private fun EventData.apply(eventId: String, op: Operation): EventData =
    when (op) {
        is Operation.RegisterItem ->
            if (op.item.eventId != eventId) this
            else copy(items = (items.filterNot { it.id == op.item.id } +
                op.item).sortedWith(CafeOrder.items))
        is Operation.AssignSlot -> copy(slots = slots.map { slot ->
            if (slot.id == op.slotId) slot.copy(itemId = op.itemId) else slot
        })
        is Operation.LogOutcome ->
            if (items.none { it.id == op.outcome.itemId }) this
            else copy(outcomes = (outcomes.filterNot { it.id == op.outcome.id } +
                op.outcome).sortedWith(CafeOrder.outcomes))
    }

/** Applies one local change to the cache, as [withPending] does. */
internal suspend fun CafeCache.apply(op: Operation) = when (op) {
    is Operation.RegisterItem -> putItem(op.item)
    is Operation.AssignSlot -> assign(op.slotId, op.itemId)
    is Operation.LogOutcome -> putOutcome(op.outcome)
}
