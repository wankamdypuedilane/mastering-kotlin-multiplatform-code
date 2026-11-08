// ch03-gradle-build-logic/units/src/commonMain/.../Grams.kt
package hearthseed.units

import kotlin.jvm.JvmInline

/** A seed quantity in whole grams. */
@JvmInline
value class Grams(val value: Int) : Comparable<Grams> {
    init {
        require(value >= 0) { "Grams cannot be negative: $value" }
    }

    operator fun plus(other: Grams): Grams = Grams(value + other.value)

    override fun compareTo(other: Grams): Int = value.compareTo(other.value)

    override fun toString(): String = "$value g"
}

val Int.grams: Grams get() = Grams(this)

/**
 * Grams needed for [seeds] seeds, given the weight of 1,000 seeds
 * (the "thousand-seed weight" printed in seed catalogues), rounded up.
 */
fun gramsForSeeds(seeds: Int, thousandSeedWeight: Grams): Grams {
    require(seeds >= 0) { "seeds must be >= 0" }
    val milligrams = seeds.toLong() * thousandSeedWeight.value
    return Grams(((milligrams + 999) / 1000).toInt())
}
