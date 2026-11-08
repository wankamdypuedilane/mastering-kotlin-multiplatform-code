@file:JvmName("SeedMath") // Java sees the class SeedMath, not ForJavaKt

package language.interop

import java.io.IOException

/** Java gets two overloads: packetsFor(int) and packetsFor(int, int). */
@JvmOverloads
fun packetsFor(seeds: Int, seedsPerPacket: Int = 25): Int =
    (seeds + seedsPerPacket - 1) / seedsPerPacket

class SeedBank(@JvmField val name: String) { // a public Java field
    @Throws(IOException::class) // Java may catch IOException
    fun load(path: String): Int {
        if (path.isBlank()) throw IOException("no path given")
        return path.length
    }

    companion object {
        const val MAX_PACKETS = 99 // a static final field in Java

        @JvmStatic // SeedBank.standard() instead of SeedBank.Companion
        fun standard(): SeedBank = SeedBank("Larkmoor Commons")
    }
}
