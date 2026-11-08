package seedswap.core

import kotlinx.coroutines.flow.Flow

/** The board's data, as the UI layer sees it. */
interface SwapRepository {
    fun offers(): Flow<List<SwapOffer>>
    fun offer(id: String): Flow<SwapOffer?>
    suspend fun propose(proposal: Proposal): ProposalResult
}

sealed interface ProposalResult {
    data class Sent(val proposalId: String) : ProposalResult
    data object OfferNotFound : ProposalResult
}
