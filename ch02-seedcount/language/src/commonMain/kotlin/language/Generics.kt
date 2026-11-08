package language

// Section 2.6: generics.

class Shelf<T>(private val items: List<T>) {
    val size: Int get() = items.size
    fun firstOrNull(): T? = items.firstOrNull()
    fun <R> map(transform: (T) -> R): Shelf<R> = Shelf(items.map(transform))
}

interface Source<out T> {
    fun next(): T
}

interface Sink<in T> {
    fun accept(item: T)
}

class Counter : Sink<Any> {
    var seen = 0
        private set

    override fun accept(item: Any) {
        seen++
    }
}

fun <T> drain(source: Source<T>, sink: Sink<T>, times: Int) {
    repeat(times) { sink.accept(source.next()) }
}

fun varianceDemo(): List<Any> {
    val tomatoes: List<String> = listOf("Larkmoor Red", "Gold Drop")
    val anything: List<Any> = tomatoes // OK: List<out E> is covariant
    val mutable: MutableList<String> = mutableListOf("Pea")
    // val broken: MutableList<Any> = mutable // type mismatch: invariant
    return anything + mutable
}

fun <T : Comparable<T>> largest(items: List<T>): T? = items.maxOrNull()

fun <T> sortedLabel(items: List<T>): String
    where T : CharSequence, T : Comparable<T> =
    items.sorted().joinToString(" < ")

inline fun <reified T> List<Any>.countOf(): Int = count { it is T }
// listOf("Pea", 3, "Kale", true).countOf<String>() == 2
