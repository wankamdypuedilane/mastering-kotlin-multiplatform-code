package language

// Context parameters (Stable since Kotlin 2.4.0): a dependency that many
// functions need is passed implicitly by the caller's scope.
interface SowingLogger {
    fun log(message: String)
}

class ListLogger : SowingLogger {
    val lines: List<String>
        field = mutableListOf()

    override fun log(message: String) {
        lines.add(message)
    }
}

context(logger: SowingLogger)
fun sowTray(variety: String, cells: Int): Int {
    logger.log("sowed $cells cells of $variety")
    return cells
}

context(logger: SowingLogger)
fun sowBed(vararg varieties: String): Int =
    varieties.sumOf { sowTray(it, 24) } // the context is passed along

fun contextDemo(): List<String> {
    val logger = ListLogger()
    with(logger) { sowBed("Pea", "Kale") } // logger is the context
    return logger.lines
}
// contextDemo() == ["sowed 24 cells of Pea", "sowed 24 cells of Kale"]
