package seedswap.core

import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/** A fake back end: offers live in memory; `latency` simulates the network. */
class InMemorySwapRepository(
    initial: List<SwapOffer> = SampleOffers.all,
    private val latency: Duration = Duration.ZERO,
) : SwapRepository {
    private val board = MutableStateFlow(initial)
    private val sent = MutableStateFlow(emptyList<Proposal>())

    /** Every proposal received so far (for tests and exercises). */
    val proposals: StateFlow<List<Proposal>> = sent.asStateFlow()

    override fun offers(): Flow<List<SwapOffer>> = board

    override fun offer(id: String): Flow<SwapOffer?> =
        board.map { offers -> offers.find { it.id == id } }
            .distinctUntilChanged()

    override suspend fun propose(proposal: Proposal): ProposalResult {
        delay(latency)
        if (board.value.none { it.id == proposal.offerId }) {
            return ProposalResult.OfferNotFound
        }
        board.update { offers ->
            offers.map {
                if (it.id != proposal.offerId) it
                else it.copy(proposals = it.proposals + 1)
            }
        }
        val all = sent.updateAndGet { it + proposal }
        return ProposalResult.Sent(proposalId = "p${all.size}")
    }
}
