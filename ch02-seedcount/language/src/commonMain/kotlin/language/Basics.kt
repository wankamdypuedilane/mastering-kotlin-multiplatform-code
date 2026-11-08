package language

// Section 2.1: values, variables, types and null safety.

const val SEEDS_PER_PACKET = 25 // compile-time constant

fun inventoryLine(): String {
    val variety = "Tomato" // read-only; the type String is inferred
    var packets = 3 // mutable; the type Int is inferred
    packets += 2
    val grams: Long = 12 // explicit type
    return "$variety: $packets packets, ${grams * 2} g"
}
// inventoryLine() == "Tomato: 5 packets, 24 g"

fun labelFor(name: String?): String =
    name?.trim()?.uppercase() ?: "UNLABELED"

fun lengthOrFail(name: String?): Int = name!!.length

fun doubled(count: Int?): Int {
    if (count == null) return 0
    return count * 2 // smart cast: count is an Int here
}

fun weightLabel(value: Any): String =
    if (value is Int && value > 0) "$value g" else "unknown"

fun sizeOf(value: Any): Int {
    val text = value as? String ?: return -1 // safe cast
    return text.length
}
// labelFor("  gold drop ") == "GOLD DROP"   labelFor(null) == "UNLABELED"
// lengthOrFail(null) throws NullPointerException

fun schemaLine(count: Int): String =
    $$"""{"$schema": "seed-stock", "count": $$count}"""
// schemaLine(4) == {"$schema": "seed-stock", "count": 4}
