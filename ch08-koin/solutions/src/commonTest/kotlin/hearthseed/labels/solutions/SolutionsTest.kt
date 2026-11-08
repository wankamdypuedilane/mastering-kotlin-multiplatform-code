package hearthseed.labels.solutions

import hearthseed.labels.LabelFormatter
import hearthseed.labels.coreModule
import hearthseed.labels.demoPackets
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class SolutionsTest {
    @Test
    fun compactFormatterIsResolvedByItsQualifier() {
        val koin = koinApplication { modules(coreModule, compactModule) }.koin
        val compact = koin.get<LabelFormatter>(Compact)
        assertEquals("L-042 Moon and Stars watermelon (2025)",
            compact.format(demoPackets[0]))
        // The unqualified definition is still the standard formatter.
        assertEquals(
            "Larkmoor Commons | Moon and Stars watermelon | 2025 | 12 g" +
                " | lot L-042",
            koin.get<LabelFormatter>().format(demoPackets[0]),
        )
        koin.close()
    }

    @Test
    fun detailViewModelReceivesTheLotIdAsAParameter() {
        val koin = koinApplication { modules(coreModule, detailModule) }.koin
        val viewModel = koin.get<LabelDetailViewModel> { parametersOf("L-107") }
        assertEquals("L-107", viewModel.lotId)
        assertEquals(
            "Larkmoor Commons | Cherokee Trail of Tears bean | 2024 | 30 g" +
                " | lot L-107",
            viewModel.label,
        )
        koin.close()
    }

    @Test
    fun queueSendsEveryLabelToTheSinkOfTheCaller() {
        val sent = mutableListOf<String>()
        val testSink = module { single<LabelSink> { LabelSink(sent::add) } }
        val koin = koinApplication { modules(queueModule, testSink) }.koin
        assertEquals(3, koin.get<PrintQueue>().printAll())
        assertEquals(listOf("lot L-042", "lot L-107", "lot L-118"),
            sent.map { it.substringAfterLast("| ") })
        koin.close()
    }
}
