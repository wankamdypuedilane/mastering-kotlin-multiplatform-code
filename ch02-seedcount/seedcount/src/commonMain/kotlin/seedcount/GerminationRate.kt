package seedcount

import kotlin.jvm.JvmInline

/** The share of seeds that sprout, in whole percent: always 1..100. */
@JvmInline
value class GerminationRate private constructor(val percent: Int) {
    companion object {
        fun of(percent: Int): GerminationRate? =
            if (percent in 1..100) GerminationRate(percent) else null

        /** Rate measured by sowing [sown] seeds and counting [sprouted]. */
        fun fromTest(sown: Int, sprouted: Int): GerminationRate? {
            require(sown > 0) { "sown must be positive, was $sown" }
            require(sprouted in 0..sown) { "sprouted must be in 0..$sown" }
            return of(sprouted * 100 / sown)
        }
    }
}
