// ch09-ktor/client/src/commonMain/kotlin/hearthseed/network/HttpClientFactory.kt
package hearthseed.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json

const val MEMBER_TOKEN_HEADER = "X-Member-Token"

data class ApiConfig(
    /** Must end with "/" so that relative paths are appended to it. */
    val baseUrl: String = "http://127.0.0.1:8085/api/v1/",
    val requestTimeoutMillis: Long = 10_000,
    val connectTimeoutMillis: Long = 5_000,
    val maxRetries: Int = 2,
    val retryBaseDelayMillis: Long = 500,
    val logLevel: LogLevel = LogLevel.NONE,
    val logger: Logger = Logger.SIMPLE,
) {
    init {
        require(baseUrl.endsWith("/")) { "baseUrl must end with '/': $baseUrl" }
    }
}

val HearthseedJson = Json { ignoreUnknownKeys = true }

private val idempotent =
    setOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Put, HttpMethod.Delete)

/** With [engine] = null, Ktor picks the default engine of the target. */
fun createHttpClient(
    config: ApiConfig,
    engine: HttpClientEngine? = null,
): HttpClient {
    val setup: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true // non-2xx responses throw ResponseException
        install(ContentNegotiation) { json(HearthseedJson) }
        // Retry before HttpTimeout, so that timeouts can be retried too.
        install(HttpRequestRetry) {
            maxRetries = config.maxRetries
            retryIf { request, response ->
                request.method in idempotent && response.status.value >= 500
            }
            retryOnExceptionIf { request, cause ->
                request.method in idempotent && cause !is CancellationException
            }
            exponentialDelay(
                baseDelayMs = config.retryBaseDelayMillis,
                randomizationMs = config.retryBaseDelayMillis / 2,
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            connectTimeoutMillis = config.connectTimeoutMillis
        }
        install(Logging) {
            logger = config.logger
            level = config.logLevel
            sanitizeHeader { it.equals(MEMBER_TOKEN_HEADER, ignoreCase = true) }
        }
        defaultRequest { url(config.baseUrl) }
    }
    return if (engine == null) HttpClient(setup) else HttpClient(engine, setup)
}
