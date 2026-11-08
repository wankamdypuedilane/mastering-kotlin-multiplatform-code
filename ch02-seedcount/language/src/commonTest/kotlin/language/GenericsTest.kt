package language

import kotlin.test.Test
import kotlin.test.assertEquals

class GenericsTest {
    @Test
    fun genericClass() {
        val shelf = Shelf(listOf("Pea", "Leek"))
        val lengths: Shelf<Int> = shelf.map { it.length }
        assertEquals(3, lengths.firstOrNull())
        assertEquals(2, lengths.size)
    }

    @Test
    fun variance() {
        val packets = object : Source<String> {
            override fun next() = "packet"
        }
        val counter = Counter()
        drain(packets, counter, times = 3) // Sink<Any> accepted as Sink<String>
        assertEquals(3, counter.seen)
        assertEquals(3, varianceDemo().size)
    }

    @Test
    fun constraintsAndReified() {
        assertEquals(75, largest(listOf(60, 75, 55)))
        val names = listOf("Pea", "Kale", "Leek")
        assertEquals("Kale < Leek < Pea", sortedLabel(names))
        val mixed: List<Any> = listOf("Pea", 3, "Kale", 4.5)
        assertEquals(2, mixed.countOf<String>())
        assertEquals(1, mixed.countOf<Int>())
    }
}
