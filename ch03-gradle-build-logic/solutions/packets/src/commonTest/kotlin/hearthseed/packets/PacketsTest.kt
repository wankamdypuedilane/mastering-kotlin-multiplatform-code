// ch03-gradle-build-logic/solutions/packets/src/commonTest/.../PacketsTest.kt
package hearthseed.packets

import hearthseed.units.grams
import kotlin.test.Test
import kotlin.test.assertEquals

class PacketsTest {
    @Test
    fun roundsUpToWholePackets() {
        assertEquals(3, packetsFor(25.grams, packetSize = 10.grams))
        assertEquals(2, packetsFor(20.grams, packetSize = 10.grams))
        assertEquals(0, packetsFor(0.grams, packetSize = 10.grams))
    }
}
