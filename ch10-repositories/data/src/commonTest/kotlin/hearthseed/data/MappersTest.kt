package hearthseed.data

import hearthseed.data.fakes.SampleData
import hearthseed.data.model.Difficulty
import hearthseed.data.model.LotStatus
import hearthseed.data.remote.InvalidDataException
import hearthseed.data.remote.VarietyDto
import hearthseed.data.remote.toDomain
import hearthseed.data.remote.toDomainSkippingInvalid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class MappersTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesServerJsonAndIgnoresUnknownKeys() {
        val body = """
            {"id":"v-kale-01","commonName":" Larkmoor Winter Kale ",
             "latinName":"Brassica oleracea","family":"Brassicaceae",
             "daysToMaturity":55,"sowingMonths":[3,4,7,8,4],
             "difficulty":"EASY","imageUrl":"/img/kale.png"}
        """.trimIndent()
        val variety = json.decodeFromString<VarietyDto>(body).toDomain()
        assertEquals("Larkmoor Winter Kale", variety.commonName)
        assertEquals(setOf(3, 4, 7, 8), variety.sowingMonths)
        assertEquals(Difficulty.EASY, variety.difficulty)
        assertEquals("", variety.description)
    }

    @Test
    fun rejectsInvalidVarieties() {
        assertFailsWith<InvalidDataException> {
            SampleData.kale.copy(sowingMonths = listOf(0, 13)).toDomain()
        }
        assertFailsWith<InvalidDataException> {
            SampleData.kale.copy(difficulty = "EXPERT").toDomain()
        }
        assertFailsWith<InvalidDataException> {
            SampleData.kale.copy(daysToMaturity = 0).toDomain()
        }
    }

    @Test
    fun listMappingDropsOnlyTheInvalidItems() {
        val broken = SampleData.kale.copy(id = "", commonName = "")
        val dtos = SampleData.varieties + broken
        assertEquals(3, dtos.toDomainSkippingInvalid().size)
    }

    @Test
    fun mapsLots() {
        val lot = SampleData.lots.getValue("v-kale-01")[1].toDomain()
        assertEquals(LotStatus.ON_LOAN, lot.status)
        assertNull(lot.donorMemberId)
        assertFailsWith<InvalidDataException> {
            val lot = SampleData.lots.getValue("v-kale-01")[0]
            lot.copy(germinationRate = 120).toDomain()
        }
    }
}
