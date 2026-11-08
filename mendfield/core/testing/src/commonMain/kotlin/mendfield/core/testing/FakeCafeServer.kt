// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/FakeCafeServer.kt
package mendfield.core.testing

import mendfield.core.data.CafeRemote
import mendfield.core.data.EventData
import mendfield.core.domain.AssignmentProblem
import mendfield.core.domain.checkAssignment
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.network.ApiError
import mendfield.core.network.ApiResult
import mendfield.core.sync.Operation
import mendfield.core.sync.OperationResult
import mendfield.core.sync.PendingOperation
import mendfield.core.sync.PushResult
import mendfield.core.sync.SyncTransport

/**
 * The café server in miniature, for tests of repositories, ViewModels and
 * whole-app flows: it serves reads ([CafeRemote]) and applies outbox batches
 * ([SyncTransport]) with the rules of :core:domain, first writer wins, and
 * answers a replayed operation id with the stored result.
 */
class FakeCafeServer(
    var events: List<Event> = SampleData.events,
    var fixers: List<Fixer> = SampleData.fixers,
    data: Map<String, EventData> = mapOf(
        SampleData.october.id to SampleData.octoberData,
        SampleData.november.id to SampleData.novemberData,
    ),
) : CafeRemote, SyncTransport {
    /** False: every call fails like a dropped Wi-Fi. */
    var online = true
    val data = data.toMutableMap()
    val results = mutableMapOf<String, OperationResult>()
    var reads = 0
        private set
    var pushes = 0
        private set

    override suspend fun events() = read { events }
    override suspend fun fixers() = read { fixers }
    override suspend fun eventData(eventId: String): ApiResult<EventData> {
        val known = data[eventId]
        return if (online && known == null) {
            ApiResult.Failure(ApiError.NotFound("No event $eventId"))
        } else read { known!! }
    }

    override suspend fun isReachable() = online

    override suspend fun push(batch: List<PendingOperation>): PushResult {
        pushes++
        if (!online) return PushResult.Transient("offline")
        return PushResult.Delivered(batch.map { op ->
            results.getOrPut(op.id) { apply(op) }
        })
    }

    /** Another organizer's device registered an item. */
    fun registerDirectly(item: Item) {
        val d = data.getValue(item.eventId)
        data[item.eventId] = d.copy(items = d.items + item)
    }

    /** Another organizer's device got there first. */
    fun assignDirectly(slotId: String, itemId: String) {
        val eventId = slotId.substringBefore('.')
        val d = data.getValue(eventId)
        data[eventId] = d.copy(slots = d.slots.map {
            if (it.id == slotId) it.copy(itemId = itemId) else it
        })
    }

    private fun apply(op: PendingOperation): OperationResult {
        fun rejected(code: String) =
            OperationResult.Rejected(op.id, code, "Refused: $code")
        when (val o = op.operation) {
            is Operation.RegisterItem -> {
                val d = data[o.item.eventId] ?: return rejected("not_found")
                val same = d.items.firstOrNull { it.id == o.item.id }
                if (same != null && same != o.item) return rejected("id_conflict")
                if (same == null) data[o.item.eventId] = d.copy(items = d.items + o.item)
            }
            is Operation.AssignSlot -> {
                val eventId = o.slotId.substringBefore('.')
                val d = data[eventId] ?: return rejected("not_found")
                val slot = d.slots.firstOrNull { it.id == o.slotId }
                val item = d.items.firstOrNull { it.id == o.itemId }
                if (slot == null || item == null) return rejected("not_found")
                when (checkAssignment(slot, item, d.slots, d.items)) {
                    null -> assignDirectly(o.slotId, o.itemId)
                    is AssignmentProblem.SlotTaken -> return rejected("slot_taken")
                    is AssignmentProblem.ItemAlreadyScheduled ->
                        return rejected("item_scheduled")
                    is AssignmentProblem.VisitorBusy -> return rejected("visitor_busy")
                    AssignmentProblem.WrongEvent -> return rejected("wrong_event")
                }
            }
            is Operation.LogOutcome -> {
                val entry = data.entries.firstOrNull { (_, d) ->
                    d.items.any { it.id == o.outcome.itemId }
                } ?: return rejected("not_found")
                val d = entry.value
                if (d.outcomes.none { it.id == o.outcome.id }) {
                    data[entry.key] = d.copy(outcomes = d.outcomes + o.outcome)
                }
            }
        }
        return OperationResult.Applied(op.id)
    }

    private inline fun <T> read(value: () -> T): ApiResult<T> {
        reads++
        return if (online) ApiResult.Success(value())
        else ApiResult.Failure(ApiError.Network("offline"))
    }
}
