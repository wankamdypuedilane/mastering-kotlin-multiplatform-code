// ch22-release/solutions/src/jvmMain/.../StaticSiteServer.kt
package release.solutions

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import release.cacheControlFor
import release.contentTypeFor
import release.isCompressible
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream

/** Serves a web distribution with the headers a real host should send. */
class StaticSiteServer(root: Path, port: Int = 0) : AutoCloseable {
    private val root: Path = root.toAbsolutePath().normalize()
    private val server =
        HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    val port: Int get() = server.address.port

    init {
        server.createContext("/") { exchange -> exchange.use { serve(it) } }
        server.start()
    }

    private fun serve(exchange: HttpExchange) {
        val requested = exchange.requestURI.path.removePrefix("/")
            .ifEmpty { "index.html" }
        val file = root.resolve(requested).normalize()
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            exchange.sendResponseHeaders(404, -1)
            return
        }
        var body = Files.readAllBytes(file)
        val headers = exchange.responseHeaders
        headers["Content-Type"] = contentTypeFor(requested)
        headers["Cache-Control"] = cacheControlFor(requested)
        val acceptsGzip = exchange.requestHeaders["Accept-Encoding"].orEmpty()
            .any { "gzip" in it }
        if (acceptsGzip && isCompressible(requested)) {
            body = gzip(body)
            headers["Content-Encoding"] = "gzip"
            headers["Vary"] = "Accept-Encoding"
        }
        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseBody.write(body)
    }

    private fun gzip(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(bytes) }
        return out.toByteArray()
    }

    override fun close() = server.stop(0)
}

/** `./gradlew :solutions:serveWeb` serves the production web build. */
fun main(args: Array<String>) {
    val root = Path.of(
        args.firstOrNull() ?: "webApp/build/dist/wasmJs/productionExecutable")
    val server = StaticSiteServer(root, port = 8080)
    println("Serving $root on http://127.0.0.1:${server.port}/ (Ctrl+C stops)")
}
