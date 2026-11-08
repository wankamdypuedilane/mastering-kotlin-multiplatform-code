package language

// Section 2.5: interfaces, inheritance and delegation.

interface Sowable {
    val name: String
    val sowingMonths: Set<Int>
    fun canSowIn(month: Int): Boolean = month in sowingMonths
}

abstract class Crop(override val name: String) : Sowable {
    abstract val daysToMaturity: Int
    open fun describe(): String = "$name, $daysToMaturity days"
}

class Bean(override val sowingMonths: Set<Int>) : Crop("Bean") {
    override val daysToMaturity = 60
    override fun describe(): String = super.describe() + ", climbing"
}
// Bean(setOf(5, 6)).describe() == "Bean, 60 days, climbing"

interface Inventory {
    fun count(variety: String): Int
    fun add(variety: String, packets: Int)
}

class MapInventory : Inventory {
    private val packets = mutableMapOf<String, Int>()
    override fun count(variety: String): Int = packets[variety] ?: 0
    override fun add(variety: String, packets: Int) {
        this.packets[variety] = count(variety) + packets
    }
}

class AuditedInventory(private val inner: Inventory) : Inventory by inner {
    val audit: List<String>
        field = mutableListOf()

    override fun add(variety: String, packets: Int) {
        audit.add("+$packets $variety")
        inner.add(variety, packets)
    }
}

class SeasonReport(private val lines: List<String>) {
    var renders = 0
        private set

    val text: String by lazy {
        renders++
        lines.joinToString("\n")
    }
}
