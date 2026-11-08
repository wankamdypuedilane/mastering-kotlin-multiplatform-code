// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/Entities.kt
package mendfield.core.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.time.Instant
import mendfield.core.model.ItemCategory
import mendfield.core.model.OutcomeKind
import mendfield.core.model.Skill

// The device copy of the server's data. No foreign keys: a refresh replaces
// one event's rows in any order, inside one transaction.

/** Dates and wall-clock times as ISO text, which sorts like the values. */
@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val venue: String,
    val date: String,                                   // "2026-11-21"
    @ColumnInfo(name = "opens_at") val opensAt: String, // "10:00"
    @ColumnInfo(name = "closes_at") val closesAt: String,
    @ColumnInfo(name = "time_zone") val timeZone: String, // "Europe/London"
)

@Entity(tableName = "fixer")
data class FixerEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val skills: Set<Skill>, // "ELECTRICAL,SEWING" (Converters)
)

// Every board query filters by event: index the column it filters on.
@Entity(tableName = "item", indices = [Index("event_id")])
data class ItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "event_id") val eventId: String,
    val category: ItemCategory, // enums are stored by name, as TEXT
    val description: String,
    @ColumnInfo(name = "visitor_name") val visitorName: String,
    @ColumnInfo(name = "registered_at") val registeredAt: Instant,
)

@Entity(tableName = "slot", indices = [Index("event_id")])
data class SlotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "event_id") val eventId: String,
    @ColumnInfo(name = "fixer_id") val fixerId: String,
    @ColumnInfo(name = "start_at") val start: Instant, // "end" is SQL
    @ColumnInfo(name = "end_at") val end: Instant,
    @ColumnInfo(name = "item_id") val itemId: String?,
)

@Entity(tableName = "outcome", indices = [Index("item_id")])
data class OutcomeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    val kind: OutcomeKind,
    val note: String,
    @ColumnInfo(name = "logged_at") val loggedAt: Instant,
)

/**
 * The outbox: one row per operation, in arrival order ([seq]). [payload] is
 * OperationCodec's JSON, the body the server receives; a row with a
 * [rejectedCode] is an operation the server refused.
 */
@Entity(tableName = "outbox", indices = [Index("op_id", unique = true)])
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    @ColumnInfo(name = "op_id") val opId: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    val payload: String,
    @ColumnInfo(name = "rejected_code") val rejectedCode: String? = null,
    @ColumnInfo(name = "rejected_message") val rejectedMessage: String? = null,
)
