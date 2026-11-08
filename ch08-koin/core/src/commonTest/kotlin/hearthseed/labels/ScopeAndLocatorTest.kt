// ch08-koin/core/src/commonTest/kotlin/hearthseed/labels/ScopeAndLocatorTest.kt
package hearthseed.labels

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.error.ClosedScopeException
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScopeAndLocatorTest {
    @Test
    fun eachPrintRunHasItsOwnNumbering() {
        val koin = koinApplication { modules(coreModule, printRunModule) }.koin
        val run1 = koin.createScope<PrintRun>("run-1")
        assertEquals("#1", run1.get<NumberedLabels>().next("L-042")?.take(2))
        assertEquals("#2", run1.get<NumberedLabels>().next("L-107")?.take(2))

        val run2 = koin.createScope<PrintRun>("run-2")   // a fresh instance
        assertEquals("#1", run2.get<NumberedLabels>().next("L-118")?.take(2))

        run1.close()                                     // the run is over
        assertFailsWith<ClosedScopeException> { run1.get<NumberedLabels>() }
        assertFails { koin.get<NumberedLabels>() }       // not in the root
        koin.close()
    }

    @Test
    fun aServiceLocatorNeedsTheGlobalKoin() {
        val printer = LabelPrinter(DemoPacketRepository(), StandardLabelFormatter("T"))
        assertEquals(3, printer.allLabels().size)       // DI: just call the constructor

        assertFails { LocatorPrinter().allLabels() }   // locator: no global Koin yet
        startKoin { modules(coreModule) }
        try {
            assertTrue(LocatorPrinter().allLabels().size == 3)
        } finally {
            stopKoin()
        }
    }
}
