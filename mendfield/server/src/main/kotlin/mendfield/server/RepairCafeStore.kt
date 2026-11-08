// mendfield/server/src/main/kotlin/mendfield/server/RepairCafeStore.kt
package mendfield.server

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import mendfield.core.domain.AssignmentProblem
import mendfield.core.domain.OutcomeStats
import mendfield.core.domain.checkAssignment
import mendfield.core.domain.outcomeStats
import mendfield.core.domain.slotGrid
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot
import mendfield.core.model.Skill

/** The in-memory Brindle Street Repair Café. A restart resets it. */
class RepairCafeStore internal constructor(
    dataset: Dataset,
    private val clock: Clock,
) {
    private val lock = Any()
    private val fixers = dataset.fixers.map { dto ->
        Fixer(dto.id, dto.displayName, dto.skills.map(Skill::valueOf).toSet())
    }
    private val events: Map<String, Event> =
        dataset.events.associate { it.event.id to it.event.toModel() }
    private val slots = LinkedHashMap<String, RepairSlot>()
    private val items = LinkedHashMap<String, Item>()
    private val outcomes = mutableListOf<Outcome>()
    private val sync = SyncLog()

    init {
        for (seed in dataset.events) {
            val event = events.getValue(seed.event.id)
            val length = seed.slotMinutes.minutes
            for (fixerId in seed.fixerIds) {
                slotGrid(event, fixerId, length).forEach { slots[it.id] = it }
            }
        }
        // The seed data goes through the same rules as a client would.
        for ((item, slotId) in dataset.items) {
            registerItem(item.toNewItem(), registeredAt = item.registeredAt)
            slotId?.let { assign(it, item.id) }
        }
        for (outcome in dataset.outcomes) {
            logOutcome(outcome.toNewOutcome(), loggedAt = outcome.loggedAt)
        }
    }

    val eventCount: Int get() = events.size

    fun events(): List<Event> = events.values.sortedBy { it.date }

    fun event(id: String): Event = events[id] ?: throw NotFound("Event $id")

    fun fixers(): List<Fixer> = fixers

    fun slotsOf(eventId: String): List<RepairSlot> = synchronized(lock) {
        event(eventId)
        slots.values.filter { it.eventId == eventId }
    }

    fun itemsOf(eventId: String): List<Item> = synchronized(lock) {
        event(eventId)
        items.values.filter { it.eventId == eventId }
    }

    fun outcomesOf(eventId: String): List<Outcome> = synchronized(lock) {
        val ids = itemsOf(eventId).map { it.id }.toSet()
        outcomes.filter { it.itemId in ids }
    }

    /** The same function the apps use offline. */
    fun statsOf(eventId: String): OutcomeStats = synchronized(lock) {
        outcomeStats(itemsOf(eventId), outcomesOf(eventId))
    }

    /** The item, and whether it is new: the same id and data is a no-op. */
    fun registerItem(
        new: NewItemDto,
        registeredAt: String? = null,
    ): Pair<Item, Boolean> = synchronized(lock) {
        val event = event(new.eventId)
        val item = Item(
            id = new.id?.let { validId(it, "item") }
                ?: freeId("item", items.keys),
            eventId = event.id,
            category = parseCategory(new.category),
            description = requireText(new.description, "description", 200),
            visitorName = requireText(new.visitorName, "visitorName", 80),
            registeredAt = registeredAt
                ?.let { parseInstant(it, "registeredAt") } ?: clock.now(),
        )
        val existing = items[item.id]
        when {
            existing == null -> item.also { items[it.id] = it } to true
            existing == item.copy(registeredAt = existing.registeredAt) ->
                existing to false
            else -> throw Conflict("id_conflict", "Item ${item.id} differs")
        }
    }

    fun assign(slotId: String, itemId: String): RepairSlot =
        synchronized(lock) { assignLocked(slotId, itemId) }

    private fun assignLocked(slotId: String, itemId: String): RepairSlot {
        val slot = slots[slotId] ?: throw NotFound("Slot $slotId")
        val item = items[itemId] ?: throw NotFound("Item $itemId")
        val eventSlots = slots.values.filter { it.eventId == slot.eventId }
        val eventItems = items.values.filter { it.eventId == slot.eventId }
        val problem = checkAssignment(slot, item, eventSlots, eventItems)
        if (problem != null) throw problem.toConflict(item)
        return slot.copy(itemId = item.id).also { slots[slot.id] = it }
    }

    /** The outcome, and whether it is new: the same id and data is a no-op. */
    fun logOutcome(
        new: NewOutcomeDto,
        loggedAt: String? = null,
    ): Pair<Outcome, Boolean> = synchronized(lock) {
        val item = items[new.itemId] ?: throw NotFound("Item ${new.itemId}")
        if (new.note.length > 500) throw InvalidInput("note: at most 500 chars")
        val usedIds = outcomes.map { it.id }.toSet()
        val outcome = Outcome(
            id = new.id?.let { validId(it, "outcome") }
                ?: freeId("out", usedIds),
            itemId = item.id,
            kind = parseKind(new.kind),
            note = new.note.trim(),
            loggedAt = loggedAt?.let { parseInstant(it, "loggedAt") }
                ?: clock.now(),
        )
        val existing = outcomes.firstOrNull { it.id == outcome.id }
        when {
            existing == null -> outcome.also { outcomes += it } to true
            existing == outcome.copy(loggedAt = existing.loggedAt) ->
                existing to false
            else ->
                throw Conflict("id_conflict", "Outcome ${outcome.id} differs")
        }
    }

    /** Applies a batch of outbox operations in order (see [SyncLog]). */
    fun sync(operations: List<SyncOperationDto>): List<SyncResultDto> =
        synchronized(lock) {
            operations.map { op -> sync.answer(op) { apply(op) } }
        }

    private fun apply(op: SyncOperationDto) {
        validId(op.id, "operation")
        when (op) {
            is RegisterItemOp ->
                registerItem(op.item.toNewItem(), op.item.registeredAt)
            is AssignSlotOp -> assign(op.slotId, op.itemId)
            is LogOutcomeOp ->
                logOutcome(op.outcome.toNewOutcome(), op.outcome.loggedAt)
        }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Loads src/main/resources/dataset.json. */
        fun fromResources(clock: Clock = Clock.System): RepairCafeStore {
            val text = RepairCafeStore::class.java
                .getResource("/dataset.json")!!.readText()
            return RepairCafeStore(json.decodeFromString<Dataset>(text), clock)
        }
    }
}

private fun AssignmentProblem.toConflict(item: Item): Conflict = when (this) {
    is AssignmentProblem.SlotTaken ->
        Conflict("slot_taken", "The slot holds $itemId")
    is AssignmentProblem.ItemAlreadyScheduled ->
        Conflict("item_scheduled", "Item ${item.id} is in $slotId")
    is AssignmentProblem.VisitorBusy ->
        Conflict("visitor_busy", "${item.visitorName} is busy then ($slotId)")
    AssignmentProblem.WrongEvent ->
        Conflict("wrong_event", "Item ${item.id} is for another event")
}

/** "item-015": the first free number after the ones in use. */
private fun freeId(prefix: String, used: Set<String>): String =
    generateSequence(used.size + 1) { it + 1 }
        .map { "$prefix-%03d".format(it) }
        .first { it !in used }

private fun EventDto.toModel() = Event(
    id, title, venue, LocalDate.parse(date), LocalTime.parse(opensAt),
    LocalTime.parse(closesAt), TimeZone.of(timeZone),
)

private fun ItemDto.toNewItem() =
    NewItemDto(eventId, category, description, visitorName, id)

private fun OutcomeDto.toNewOutcome() = NewOutcomeDto(itemId, kind, note, id)
