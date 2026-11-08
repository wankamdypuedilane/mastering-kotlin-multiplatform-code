package hearthseed.labels

import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class CoreModuleTest {
    private val testPacket = SeedPacket("T-1", "Test pea", 2026, 5)

    // Test doubles: a later module overrides earlier definitions.
    private val testOverrides = module {
        single<PacketRepository> {
            InMemoryPacketRepository(listOf(testPacket))
        }
        single(LibraryName) { "Test Library" }
    }

    @Test
    fun overridesReplaceTheRealDefinitions() {
        val koin = koinApplication { modules(coreModule, testOverrides) }.koin
        val printer = koin.get<LabelPrinter>()
        assertEquals(listOf("Test Library | Test pea | 2026 | 5 g | lot T-1"),
            printer.allLabels())
        koin.close()
    }

    @Test
    fun singleIsSharedAndFactoryIsNew() {
        val koin = koinApplication { modules(coreModule) }.koin
        assertSame(koin.get<PacketRepository>(), koin.get<PacketRepository>())
        assertNotSame(koin.get<LabelPrinter>(), koin.get<LabelPrinter>())
        koin.close()
    }

    @Test
    fun parametersReachTheFactory() {
        val koin = koinApplication { modules(coreModule) }.koin
        val sheet = koin.get<LabelSheet> { parametersOf(2) }
        assertEquals(2, sheet.columns)
        assertEquals(listOf(2, 1), sheet.rows().map { it.size })
        koin.close()
    }
}
