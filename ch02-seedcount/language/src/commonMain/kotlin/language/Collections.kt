package language

// Section 2.8: collections and sequences.

val catalog = listOf(
    Variety("Larkmoor Red", 75, "Solanaceae"),
    Variety("Gold Drop", 65, "Solanaceae"),
    Variety("Scarlet Runner", 60, "Fabaceae"),
    Variety("Winter Kale", 55, "Brassicaceae"),
)

fun collectionBasics(): List<Any> {
    val families = setOf("Solanaceae", "Fabaceae", "Solanaceae")
    val stock = mapOf("Gold Drop" to 3, "Winter Kale" to 0)
    val wishlist = mutableListOf("Pea")
    wishlist += "Leek"
    return listOf(families.size, stock["Gold Drop"] ?: 0, wishlist)
}
// == [2, 3, [Pea, Leek]]

fun transformations(): List<Any> {
    val quick = catalog.filter { it.daysToMaturity < 70 }.map { it.name }
    val byFamily = catalog.groupBy { it.family }.mapValues { it.value.size }
    val byName = catalog.associateBy { it.name }
    val (fast, slow) = catalog.partition { it.daysToMaturity <= 60 }
    val totalDays = catalog.sumOf { it.daysToMaturity }
    val longest = catalog.maxBy { it.daysToMaturity }.name
    val sorted = catalog.sortedWith(compareBy({ it.family }, { it.name }))
// quick == [Gold Drop, Scarlet Runner, Winter Kale]
// byFamily == {Solanaceae=2, Fabaceae=1, Brassicaceae=1}
// fast.size to slow.size == (2, 2)    totalDays == 255
// longest == "Larkmoor Red"           sorted.first().name == "Winter Kale"
    return listOf(
        quick, byFamily, byName.getValue("Gold Drop").daysToMaturity,
        fast.size to slow.size, totalDays, longest, sorted.first().name,
    )
}

fun evaluationLog(lazy: Boolean): List<String> {
    val log = mutableListOf<String>()
    val numbers = listOf(1, 2, 3, 4)
    if (lazy) {
        numbers.asSequence()
            .map { log += "map $it"; it * 10 }
            .filter { log += "filter $it"; it > 15 }
            .first()
    } else {
        numbers
            .map { log += "map $it"; it * 10 }
            .filter { log += "filter $it"; it > 15 }
            .first()
    }
    return log
}

fun doublingSeeds(): List<Int> =
    generateSequence(1) { it * 2 }.takeWhile { it < 100 }.toList()
