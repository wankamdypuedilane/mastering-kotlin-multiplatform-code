package language

import kotlin.jvm.JvmInline

// Section 2.4: classes.

class SeedTray(val name: String, val cells: Int) {
    init {
        require(cells > 0) { "A tray needs at least one cell" }
    }

    var sown: Int = 0
        private set

    val free: Int
        get() = cells - sown

    fun sow(count: Int) {
        sown = minOf(cells, sown + count)
    }
}

class SowingLog {
    val entries: List<String>
        field = mutableListOf()

    fun record(entry: String) {
        entries.add(entry) // inside the class, entries is a MutableList
    }
}

data class Variety(
    val name: String,
    val daysToMaturity: Int,
    val family: String = "unknown",
)

enum class Difficulty(val stars: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    fun label(): String = "*".repeat(stars)
}

sealed interface Sowing {
    data class Indoors(val weeksBeforeFrost: Int) : Sowing
    data class Direct(val depthMm: Int) : Sowing
    data object Skip : Sowing
}

fun instructions(sowing: Sowing): String = when (sowing) {
    is Sowing.Indoors if sowing.weeksBeforeFrost > 8 ->
        "start very early, under lights"
    is Sowing.Indoors -> "start indoors ${sowing.weeksBeforeFrost} weeks early"
    is Sowing.Direct -> "sow outside, ${sowing.depthMm} mm deep"
    Sowing.Skip -> "not this season"
}
// instructions(Sowing.Indoors(10)) == "start very early, under lights"
// instructions(Sowing.Indoors(6)) == "start indoors 6 weeks early"

object FrostDates {
    const val LAST_SPRING_FROST_WEEK = 16
    fun sowingWeek(weeksBeforeFrost: Int): Int =
        LAST_SPRING_FROST_WEEK - weeksBeforeFrost
}

class Packet private constructor(val seeds: Int) {
    companion object {
        const val STANDARD_SEEDS = 25
        fun standard(): Packet = Packet(STANDARD_SEEDS)
        fun of(seeds: Int): Packet? = if (seeds > 0) Packet(seeds) else null
    }
}

@JvmInline
value class Grams(val value: Int) {
    init {
        require(value >= 0) { "Weight cannot be negative" }
    }

    operator fun plus(other: Grams): Grams = Grams(value + other.value)
}
