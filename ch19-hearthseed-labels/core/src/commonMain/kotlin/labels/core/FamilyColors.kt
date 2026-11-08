package labels.core

/** Stripe color (0xAARRGGBB) per botanical family; gray for unknown ones. */
fun familyColor(family: String): Long = when (family) {
    "Solanaceae" -> 0xFFC0392B
    "Fabaceae" -> 0xFF2E7D32
    "Brassicaceae" -> 0xFF6A1B9A
    "Apiaceae" -> 0xFFE67E22
    "Amaryllidaceae" -> 0xFF8D6E63
    else -> 0xFF757575
}
