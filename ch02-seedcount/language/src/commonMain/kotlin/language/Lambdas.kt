package language

// Section 2.7: lambdas, higher-order functions and scope functions.

val double: (Int) -> Int = { it * 2 }
val add: (Int, Int) -> Int = { a, b -> a + b }

fun applyTwice(value: Int, step: (Int) -> Int): Int = step(step(value))

fun afterLosses(lossPercent: Int): (Int) -> Int =
    { seeds -> seeds * (100 - lossPercent) / 100 }

fun isEven(n: Int): Boolean = n % 2 == 0

fun lambdaDemo(): List<Any> {
    val trimmed = applyTwice(100, afterLosses(10)) // 100 -> 90 -> 81
    val names = listOf("pea", "kale").map(String::uppercase)
    val evens = (1..6).filter(::isEven)
    var total = 0
    listOf(3, 4).forEach { total += it } // the lambda captures total
    return listOf(trimmed, names, evens, total)
}

fun scopeDemo(input: String?, log: MutableList<String>): List<Any> {
    val label = input?.let { "Variety: $it" } ?: "no name"
    val tray = SeedTray("A", 24).apply { sow(10) }
    val summary = tray.run { "$name: $sown/$cells" }
    val used = with(tray) { cells - free }
    val variety = Variety("Kale", 55).also { log.add("created ${it.name}") }
    return listOf(label, tray.sown, summary, used, variety.name)
}
// scopeDemo("Leek", log) == ["Variety: Leek", 10, "A: 10/24", 10, "Kale"]
