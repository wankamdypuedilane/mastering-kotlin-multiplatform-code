// hearthseed/core/model/src/commonTest/kotlin/hearthseed/core/model/ModelTest.kt
package hearthseed.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelTest {
    private fun lot(status: LotStatus) =
        SeedLot("lot-1", "var-1", 2024, 10, 80, null, status)

    @Test fun onlyAvailableLotsAreBorrowable() {
        assertTrue(lot(LotStatus.AVAILABLE).isBorrowable)
        assertFalse(lot(LotStatus.ON_LOAN).isBorrowable)
        assertFalse(lot(LotStatus.RETIRED).isBorrowable)
    }

    @Test fun aLoanIsOpenUntilSeedIsReturned() {
        val loan = Loan("loan-1", "lot-1", "mem-1", "2026-03-02", 2026, null)
        assertTrue(loan.isOpen)
        assertFalse(loan.copy(returnedGrams = 40).isOpen)
    }

    @Test fun everyFamilyHasACommonName() {
        assertEquals(10, BotanicalFamilies.all.size)
        BotanicalFamilies.all.forEach {
            assertTrue(BotanicalFamilies.commonName(it) != it, it)
        }
    }
}
