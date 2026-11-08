package hearthseed.labels.ui

import hearthseed.labels.InMemoryPacketRepository
import hearthseed.labels.PacketRepository
import hearthseed.labels.SeedPacket
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelViewModelTest {
    private val fakes = module {
        single<PacketRepository> {
            InMemoryPacketRepository(
                listOf(SeedPacket("T-9", "Test radish", 2026, 3)),
            )
        }
        single { PrinterHost("Test bench") }
    }

    @BeforeTest
    fun start() {
        // The real graph first, then the fakes: later definitions win.
        startKoin { modules(appModules() + fakes) }
    }

    @AfterTest
    fun stop() = stopKoin()

    @Test
    fun printAllShowsLabelsFromTheFakeRepository() {
        val viewModel = KoinPlatform.getKoin().get<LabelViewModel>()
        assertEquals("Test bench", viewModel.state.value.host)
        viewModel.printAll()
        assertEquals(
            listOf("Larkmoor Commons | Test radish | 2026 | 3 g | lot T-9"),
            viewModel.state.value.labels,
        )
    }
}
