// mendfield/core/network/src/commonTest/kotlin/mendfield/core/network/MappersTest.kt
package mendfield.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind

class MappersTest {
    private val item = Item(
        id = "4f0c", eventId = "evt-02", category = ItemCategory.TOOL,
        description = "Blunt shears", visitorName = "Owen",
        registeredAt = Instant.parse("2026-11-21T10:05:00Z"),
    )

    @Test fun anItemSurvivesTheRoundTrip() {
        assertEquals(item, item.toDto().toModel())
        assertEquals("2026-11-21T10:05:00Z", item.toDto().registeredAt)
    }

    @Test fun anOutcomeSurvivesTheRoundTrip() {
        val outcome = Outcome("o", "4f0c", OutcomeKind.ADVICE, "See a clockmaker",
            Instant.parse("2026-11-21T11:30:00Z"))
        assertEquals(outcome, outcome.toDto().toModel())
    }

    @Test fun aBadInstantIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            item.toDto().copy(registeredAt = "21/11/2026").toModel()
        }
    }

    @Test fun anUnknownTimeZoneIsRejected() {
        val dto = EventDto("e", "t", "v", "2026-11-21", "10:00", "13:00", "Mars/Olympus")
        assertFailsWith<IllegalArgumentException> { dto.toModel() }
    }
}
