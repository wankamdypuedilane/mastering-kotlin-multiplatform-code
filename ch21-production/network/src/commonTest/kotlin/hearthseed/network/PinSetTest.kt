// ch21-production/network/src/commonTest/kotlin/hearthseed/network/PinSetTest.kt
package hearthseed.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PinSetTest {
    @Test
    fun aSinglePinIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            PinSet("api.larkmoor.example", listOf("sha256/YOUR_PRIMARY_PIN="))
        }
    }

    @Test
    fun pinsMustBeSha256AndDistinct() {
        assertFailsWith<IllegalArgumentException> {
            PinSet("api.larkmoor.example", listOf("sha1/A=", "sha256/B="))
        }
        assertFailsWith<IllegalArgumentException> {
            PinSet("api.larkmoor.example", listOf("sha256/A=", "sha256/A="))
        }
    }

    @Test
    fun theAppPinsAPrimaryAndABackupKey() {
        assertEquals(2, larkmoorPins.pins.size)
    }
}
