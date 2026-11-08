package seedswap.navigation

import kotlinx.serialization.Serializable

// Each destination is a type; its properties are the destination's arguments.
@Serializable data object Board
@Serializable data class OfferDetail(val offerId: String)
@Serializable data class MakeProposal(val offerId: String)
@Serializable data class ProposalSent(
    val offerId: String,
    val proposalId: String,
)
