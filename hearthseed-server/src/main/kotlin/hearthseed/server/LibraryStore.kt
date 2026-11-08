// hearthseed-server/src/main/kotlin/hearthseed/server/LibraryStore.kt
package hearthseed.server

import java.time.Clock
import java.time.LocalDate
import kotlinx.serialization.json.Json

/** The in-memory seed library. Loans change it; a restart resets it. */
class LibraryStore internal constructor(
    dataset: Dataset,
    private val clock: Clock,
) {
    private val varieties = dataset.varieties
    private val members = dataset.members.associateBy { it.id }
    private val lots = dataset.lots.associateBy { it.id }.toMutableMap()
    private val loans = dataset.loans.toMutableList()
    private val lock = Any()

    val varietyCount: Int get() = varieties.size

    fun searchVarieties(query: String?, family: String?, page: Int, pageSize: Int):
        Page<VarietyDto> {
        val matches = varieties.filter { v ->
            (query.isNullOrBlank() ||
                v.commonName.contains(query, ignoreCase = true) ||
                v.latinName.contains(query, ignoreCase = true)) &&
                (family.isNullOrBlank() || v.family.equals(family, ignoreCase = true))
        }
        val items = matches.drop((page - 1) * pageSize).take(pageSize)
        return Page(items, page, pageSize, matches.size)
    }

    fun variety(id: String): VarietyDto =
        varieties.firstOrNull { it.id == id } ?: throw NotFound("Variety $id")

    fun lotsOf(varietyId: String): List<SeedLotDto> {
        variety(varietyId) // 404 for an unknown variety
        return synchronized(lock) { lots.values.filter { it.varietyId == varietyId } }
    }

    fun loansOf(memberId: String): List<LoanDto> {
        if (memberId !in members) throw NotFound("Member $memberId")
        return synchronized(lock) { loans.filter { it.memberId == memberId } }
    }

    fun borrow(request: LoanRequest): LoanDto = synchronized(lock) {
        if (request.memberId !in members) throw NotFound("Member ${request.memberId}")
        val lot = lots[request.lotId] ?: throw NotFound("Lot ${request.lotId}")
        if (lot.status != LotStatus.AVAILABLE) {
            throw Conflict("Lot ${lot.id} is ${lot.status}, not AVAILABLE")
        }
        val today = LocalDate.now(clock)
        val loan = LoanDto(
            id = "loan-%03d".format(loans.size + 1),
            lotId = lot.id,
            memberId = request.memberId,
            borrowedOn = today.toString(),
            dueSeason = today.year,
        )
        lots[lot.id] = lot.copy(status = LotStatus.ON_LOAN)
        loans += loan
        loan
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Loads src/main/resources/dataset.json. */
        fun fromResources(clock: Clock = Clock.systemDefaultZone()): LibraryStore {
            val text = LibraryStore::class.java.getResource("/dataset.json")!!.readText()
            return LibraryStore(json.decodeFromString<Dataset>(text), clock)
        }
    }
}
