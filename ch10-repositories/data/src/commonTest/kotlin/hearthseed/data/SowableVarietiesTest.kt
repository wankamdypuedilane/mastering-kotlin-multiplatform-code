package hearthseed.data

import hearthseed.data.domain.SowableVarieties
import hearthseed.data.fakes.FakeRemoteVarietySource
import hearthseed.data.repository.VarietyRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SowableVarietiesTest {
    private val repository = VarietyRepository(FakeRemoteVarietySource())
    private val sowable = SowableVarieties(repository)

    @Test
    fun filtersByMonthAndSortsEasiestFirst() = runTest {
        repository.refresh()
        assertEquals(
            listOf("Commons Runner Bean", "Larkmoor Winter Kale"),
            sowable(month = 4).first().map { it.commonName },
        )
        assertEquals(
            listOf("Harbour Gold Tomato"),
            sowable(2).first().map { it.commonName },
        )
    }

    @Test
    fun rejectsAnInvalidMonth() {
        assertFailsWith<IllegalArgumentException> { sowable(13) }
    }
}
