// mendfield/core/network/src/commonMain/kotlin/mendfield/core/network/Dtos.kt
package mendfield.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Client copies of the Mendfield server contract (server/README.md).
// Dates, times and instants are ISO-8601 strings; enums are their names.

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val venue: String,
    val date: String,     // "2026-11-21"
    val opensAt: String,  // "10:00", wall-clock time at the venue
    val closesAt: String,
    val timeZone: String, // "Europe/London"
)

@Serializable
data class FixerDto(
    val id: String,
    val displayName: String,
    val skills: List<String>,
)

@Serializable
data class ItemDto(
    val id: String,
    val eventId: String,
    val category: String,
    val description: String,
    val visitorName: String,
    val registeredAt: String, // "2026-11-21T10:05:00Z"
)

@Serializable
data class NewItemDto(
    val eventId: String,
    val category: String,
    val description: String,
    val visitorName: String,
    val id: String? = null, // a client-generated id, or null: the server picks
)

@Serializable
data class RepairSlotDto(
    val id: String,
    val eventId: String,
    val fixerId: String,
    val start: String,
    val end: String,
    val itemId: String? = null,
)

@Serializable
data class AssignRequest(val itemId: String)

@Serializable
data class OutcomeDto(
    val id: String,
    val itemId: String,
    val kind: String,
    val note: String,
    val loggedAt: String,
)

@Serializable
data class NewOutcomeDto(
    val itemId: String,
    val kind: String,
    val note: String = "",
    val id: String? = null,
)

@Serializable
data class StatsDto(
    val eventId: String,
    val registered: Int,
    val withOutcome: Int,
    val counts: Map<String, Int>,
    val repairRatePercent: Int? = null,
)

/** One operation of the offline outbox; [id] is its idempotency key. */
@Serializable
sealed interface SyncOperationDto {
    val id: String
}

@Serializable
@SerialName("register_item")
data class RegisterItemOp(
    override val id: String,
    val item: ItemDto,
) : SyncOperationDto

@Serializable
@SerialName("assign_slot")
data class AssignSlotOp(
    override val id: String,
    val slotId: String,
    val itemId: String,
) : SyncOperationDto

@Serializable
@SerialName("log_outcome")
data class LogOutcomeOp(
    override val id: String,
    val outcome: OutcomeDto,
) : SyncOperationDto

@Serializable
data class SyncRequest(val operations: List<SyncOperationDto>)

@Serializable
enum class SyncStatus { APPLIED, REJECTED }

@Serializable
data class SyncResultDto(
    val operationId: String,
    val status: SyncStatus,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class SyncResponse(val results: List<SyncResultDto>)

@Serializable
data class ErrorDto(val code: String, val message: String)

@Serializable
data class HealthDto(val status: String, val events: Int)
