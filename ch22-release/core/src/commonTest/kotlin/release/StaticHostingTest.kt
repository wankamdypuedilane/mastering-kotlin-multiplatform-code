// ch22-release/core/src/commonTest/kotlin/release/StaticHostingTest.kt
package release

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StaticHostingTest {
    @Test
    fun wasmGetsItsOwnMimeType() {
        assertEquals("application/wasm", contentTypeFor("skiko.wasm"))
        assertEquals("application/wasm", contentTypeFor("dist/APP.WASM"))
        assertEquals("text/javascript", contentTypeFor("hearthseed.js"))
        assertEquals("text/html; charset=utf-8", contentTypeFor("index.html"))
        assertEquals("application/octet-stream", contentTypeFor("LICENSE"))
    }

    @Test
    fun onlyHashedNamesAreImmutable() {
        val forever = "public, max-age=31536000, immutable"
        assertEquals(forever, cacheControlFor("app.3f9a0c1b2d4e5f60.wasm"))
        assertEquals(forever, cacheControlFor("3f9a0c1b2d4e5f607a8b.wasm"))
        assertEquals("no-cache", cacheControlFor("index.html"))
        assertEquals("no-cache", cacheControlFor("hearthseed.js"))
        assertEquals("no-cache", cacheControlFor("skiko.wasm"))
        assertEquals("no-cache", cacheControlFor("cafe.wasm")) // too short for a hash
    }

    @Test
    fun compressesTextAndWasmOnly() {
        assertTrue(isCompressible("skiko.wasm"))
        assertTrue(isCompressible("hearthseed.js"))
        assertFalse(isCompressible("logo.png"))
    }
}
