// hearthseed/shared/src/commonTest/kotlin/hearthseed/shared/platform/PlatformActionsTest.kt
package hearthseed.shared.platform

import hearthseed.core.model.Difficulty
import hearthseed.core.model.Loan
import hearthseed.core.model.Variety
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlatformActionsTest {
    private val pea = Variety(
        id = "var-010", commonName = "Larkmoor Pea", latinName = "Pisum sativum",
        family = "Fabaceae", daysToMaturity = 60, sowingMonths = setOf(3, 4),
        difficulty = Difficulty.EASY, description = "A tall shelling pea.",
    )

    private fun loan(id: String, season: Int) =
        Loan(id, "lot-$id", "mem-003", "2026-04-02", season, returnedGrams = null)

    @Test fun shareTextNamesTheVarietyAndTheLibrary() {
        assertEquals(
            "Larkmoor Pea (Pisum sativum): ready in 60 days. " +
                "Borrow it from the Larkmoor Commons Seed Library.",
            shareText(pea),
        )
    }

    @Test fun noReminderWithoutOpenLoans() {
        assertNull(reminderText(emptyList()))
    }

    @Test fun reminderCountsLotsAndNamesTheEarliestSeason() {
        assertEquals(
            "You are growing 1 lot. Save seed for the 2026 return.",
            reminderText(listOf(loan("1", 2026))),
        )
        assertEquals(
            "You are growing 2 lots. Save seed for the 2026 return.",
            reminderText(listOf(loan("1", 2027), loan("2", 2026))),
        )
    }
}
