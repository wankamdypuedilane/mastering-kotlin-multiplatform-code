package language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InterfacesTest {
    @Test
    fun inheritance() {
        val bean = Bean(sowingMonths = setOf(5, 6))
        assertTrue(bean.canSowIn(5))
        assertFalse(bean.canSowIn(9))
        assertEquals("Bean, 60 days, climbing", bean.describe())
        val crop: Crop = bean
        assertEquals("Bean, 60 days, climbing", crop.describe())
    }

    @Test
    fun classDelegation() {
        val inventory = AuditedInventory(MapInventory())
        inventory.add("Kale", 2)
        inventory.add("Kale", 1)
        assertEquals(3, inventory.count("Kale")) // forwarded by delegation
        assertEquals(listOf("+2 Kale", "+1 Kale"), inventory.audit)
    }

    @Test
    fun lazyProperty() {
        val report = SeasonReport(listOf("Kale: 2", "Leek: 1"))
        assertEquals(0, report.renders)
        assertEquals("Kale: 2\nLeek: 1", report.text)
        report.text
        assertEquals(1, report.renders)
    }
}
