// mendfield/core/network/src/commonMain/kotlin/mendfield/core/network/HttpClientFactory.kt
package mendfield.core.network

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

const val ORGANIZER_TOKEN_HEADER = "X-Organizer-Token"

data class ApiConfig(
    /** Must end with "/" so that relative paths are appended to it. */
    val baseUrl: String = "http://127.0.0.1:8086/api/v1/",
    val requestTimeoutMillis: Long = 10_000,
    val connectTimeoutMillis: Long = 5_000,
    /** Retries of GET requests; the outbox retries POST /sync itself. */
    val maxRetries: Int = 2,
    val retryBaseDelayMillis: Long = 500,
    val logLevel: LogLevel = LogLevel.NONE,
    val logger: Logger = Logger.SIMPLE,
) {
    init {
        require(baseUrl.endsWith("/")) { "baseUrl must end with '/': $baseUrl" }
    }
}

val MendfieldJson = Json { ignoreUnknownKeys = true }

/** With [engine] = null, Ktor picks the default engine of the target. */
fun createHttpClient(
    config: ApiConfig,
    engine: HttpClientEngine? = null,
): HttpClient {
    val setup: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true // non-2xx responses throw ResponseException
        install(ContentNegotiation) { json(MendfieldJson) }
        install(HttpRequestRetry) {
            maxRetries = config.maxRetries
            retryIf { request, response ->
                request.method == HttpMethod.Get &&
                    response.status.value >= 500
            }
            retryOnExceptionIf { request, cause ->
                request.method == HttpMethod.Get &&
                    cause !is CancellationException
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
            sanitizeHeader { name ->
                name.equals(ORGANIZER_TOKEN_HEADER, ignoreCase = true)
            }
        }
        defaultRequest { url(config.baseUrl) }
    }
    return if (engine == null) HttpClient(setup) else HttpClient(engine, setup)
}
