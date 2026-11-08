package seedswap.core

/** A member offers saved seed and says what they hope to get back. */
data class SwapOffer(
    val id: String,
    val variety: String,
    val family: String,
    val grams: Int,
    val wanted: String,
    val member: String,
    val proposals: Int = 0,
)

/** A validated answer to an offer: "I give you these seeds in exchange". */
data class Proposal(
    val offerId: String,
    val member: String,
    val variety: String,
    val grams: Int,
    val message: String,
)
