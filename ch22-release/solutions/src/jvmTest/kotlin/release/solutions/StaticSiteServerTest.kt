// ch22-release/solutions/src/jvmTest/kotlin/release/solutions/StaticSiteServerTest.kt
package release.solutions

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StaticSiteServerTest {
    private val wasm = byteArrayOf(0, 0x61, 0x73, 0x6d, 1, 0, 0, 0) + ByteArray(4096)
    private val root = Files.createTempDirectory("dist").also {
        Files.writeString(it.resolve("index.html"), "<!DOCTYPE html><title>Hearthseed</title>")
        Files.write(it.resolve("app.3f9a0c1b2d4e5f60.wasm"), wasm)
    }
    private val client = HttpClient.newHttpClient()

    private fun get(server: StaticSiteServer, path: String, gzip: Boolean = false) =
        client.send(
            HttpRequest.newBuilder(URI("http://127.0.0.1:${server.port}$path"))
                .apply { if (gzip) header("Accept-Encoding", "gzip") }.build(),
            HttpResponse.BodyHandlers.ofByteArray(),
        )

    @Test
    fun indexIsRevalidated() = StaticSiteServer(root).use { server ->
        val response = get(server, "/")
        assertEquals(200, response.statusCode())
        assertEquals("text/html; charset=utf-8", response.headers().firstValue("Content-Type").get())
        assertEquals("no-cache", response.headers().firstValue("Cache-Control").get())
    }

    @Test
    fun hashedWasmIsImmutableAndCompressed() = StaticSiteServer(root).use { server ->
        val response = get(server, "/app.3f9a0c1b2d4e5f60.wasm", gzip = true)
        val headers = response.headers()
        assertEquals("application/wasm", headers.firstValue("Content-Type").get())
        assertEquals("public, max-age=31536000, immutable", headers.firstValue("Cache-Control").get())
        assertEquals("gzip", headers.firstValue("Content-Encoding").get())
        assertContentEquals(wasm, GZIPInputStream(response.body().inputStream()).readBytes())
    }

    @Test
    fun plainRequestIsNotCompressed() = StaticSiteServer(root).use { server ->
        val response = get(server, "/app.3f9a0c1b2d4e5f60.wasm")
        assertNull(response.headers().firstValue("Content-Encoding").orElse(null))
        assertContentEquals(wasm, response.body())
    }

    @Test
    fun missingAndEscapingPathsAre404() = StaticSiteServer(root).use { server ->
        assertEquals(404, get(server, "/nope.js").statusCode())
        assertEquals(404, get(server, "/../etc/passwd").statusCode())
    }
}
