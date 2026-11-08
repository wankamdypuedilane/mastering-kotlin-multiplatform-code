// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/SampleData.kt
package mendfield.core.testing

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import mendfield.core.data.CafeOrder
import mendfield.core.data.EventData
import mendfield.core.domain.slotGrid
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.model.Skill

/**
 * A small café for tests. UTC keeps every target identical (region zones
 * need @js-joda/timezone on JS and Wasm); November in London is UTC anyway.
 */
object SampleData {
    val zone = TimeZone.UTC
    val october = Event("evt-01", "October Repair Café", "Community Hall",
        LocalDate(2026, 10, 17), LocalTime(10, 0), LocalTime(13, 0), zone)
    val november = Event("evt-02", "November Repair Café", "Community Hall",
        LocalDate(2026, 11, 21), LocalTime(10, 0), LocalTime(12, 0), zone)
    val events = listOf(october, november)

    val maud = Fixer("fx-01", "Maud Okafor", setOf(Skill.ELECTRICAL, Skill.ELECTRONICS))
    val tomasz = Fixer("fx-02", "Tomasz Wren", setOf(Skill.MECHANICAL, Skill.BICYCLES))
    val ines = Fixer("fx-03", "Ines Halloran", setOf(Skill.SEWING))
    val fixers = listOf(ines, maud, tomasz) // by name, as the cache lists them

    /** 10:00 on the November day, plus [minutes]. */
    fun at(minutes: Int): Instant =
        Instant.parse("2026-11-21T10:00:00Z") + minutes.minutes

    /** Four 30-minute slots per fixer (evt-02.fx-01.1 ... .4), cache order. */
    val novemberSlots = fixers.flatMap { slotGrid(november, it.id, 30.minutes) }
        .sortedWith(CafeOrder.slots)

    val lamp = Item("item-001", "evt-02", ItemCategory.LAMP,
        "Desk lamp flickers", "Ada Mensah", at(-60))
    val jacket = Item("item-002", "evt-02", ItemCategory.CLOTHING,
        "Broken zip", "Kofi Asante", at(-50))

    /** The lamp is in Maud's first slot; the jacket waits. */
    val novemberData = EventData(
        slots = novemberSlots.map {
            if (it.id == "evt-02.fx-01.1") it.copy(itemId = lamp.id) else it
        },
        items = listOf(lamp, jacket),
    )

    val toaster = Item("item-010", "evt-01", ItemCategory.SMALL_APPLIANCE,
        "Toaster dead", "Gwen Arbuthnot",
        Instant.parse("2026-10-17T08:30:00Z"))
    val toasterFixed = Outcome("out-001", toaster.id, OutcomeKind.FIXED,
        "New fuse", Instant.parse("2026-10-17T09:40:00Z"))
    val octoberData = EventData(
        slots = slotGrid(october, maud.id, 30.minutes)
            .mapIndexed { i, s -> if (i == 0) s.copy(itemId = toaster.id) else s },
        items = listOf(toaster),
        outcomes = listOf(toasterFixed),
    )
}
