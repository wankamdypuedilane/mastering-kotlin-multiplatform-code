package seedswap.core

/** Fictional offers from members of the Larkmoor Commons Seed Library. */
object SampleOffers {
    val all: List<SwapOffer> = listOf(
        SwapOffer("o1", "Brandywine tomato", "Solanaceae", 3,
            wanted = "Climbing beans", member = "Ada Fenwick"),
        SwapOffer("o2", "Scarlet runner bean", "Fabaceae", 120,
            wanted = "Any winter squash", member = "Tomas Reyes"),
        SwapOffer("o3", "Golden zucchini", "Cucurbitaceae", 20,
            wanted = "Lettuce", member = "Mira Okafor"),
        SwapOffer("o4", "Red oak-leaf lettuce", "Asteraceae", 5,
            wanted = "Basil or dill", member = "Jonah Pell"),
        SwapOffer("o5", "Lazy housewife bean", "Fabaceae", 80,
            wanted = "Paste tomatoes", member = "Ines Duval"),
    )
}
