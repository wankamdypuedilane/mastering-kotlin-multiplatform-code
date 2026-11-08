// hearthseed/core/model/src/commonMain/kotlin/hearthseed/core/model/BotanicalFamilies.kt
package hearthseed.core.model

/**
 * The families of the catalog, for the family filter. The API has no
 * endpoint that lists them; its README documents this fixed set.
 */
object BotanicalFamilies {
    val all: List<String> = listOf(
        "Solanaceae", "Fabaceae", "Cucurbitaceae", "Brassicaceae", "Apiaceae",
        "Asteraceae", "Amaranthaceae", "Amaryllidaceae", "Poaceae", "Lamiaceae",
    )

    /** The everyday name shown next to the botanical one. */
    fun commonName(family: String): String = when (family) {
        "Solanaceae" -> "Nightshades"
        "Fabaceae" -> "Legumes"
        "Cucurbitaceae" -> "Squashes"
        "Brassicaceae" -> "Cabbages"
        "Apiaceae" -> "Carrots"
        "Asteraceae" -> "Daisies"
        "Amaranthaceae" -> "Beets"
        "Amaryllidaceae" -> "Onions"
        "Poaceae" -> "Grasses"
        "Lamiaceae" -> "Mints"
        else -> family
    }
}
