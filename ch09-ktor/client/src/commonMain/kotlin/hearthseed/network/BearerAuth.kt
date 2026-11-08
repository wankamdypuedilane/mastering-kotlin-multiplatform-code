// ch09-ktor/client/src/commonMain/kotlin/hearthseed/network/BearerAuth.kt
package hearthseed.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

// hearthseed-server checks X-Member-Token only. This file shows the usual
// alternative of real APIs, an OAuth-style pair of bearer tokens, and is
// tested against a MockEngine (BearerAuthTest).

@Serializable
data class TokenPair(val accessToken: String, val refreshToken: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

/** Where the app keeps its tokens; Chapter 21 stores them securely. */
interface TokenStore {
    suspend fun load(): TokenPair?
    suspend fun save(tokens: TokenPair)
}

fun HttpClientConfig<*>.installBearerAuth(store: TokenStore) {
    install(Auth) {
        bearer {
            // Sent as "Authorization: Bearer <access token>" on every request.
            loadTokens { store.load()?.toBearer() }
            // Called after a 401: trade the refresh token for a new pair.
            refreshTokens {
                val refresh = oldTokens?.refreshToken
                    ?: return@refreshTokens null
                val fresh: TokenPair = client.post("auth/refresh") {
                    markAsRefreshTokenRequest() // no token, no refresh loop
                    contentType(ContentType.Application.Json)
                    setBody(RefreshRequest(refresh))
                }.body()
                store.save(fresh)
                fresh.toBearer() // the failed request is sent again with it
            }
        }
    }
}

private fun TokenPair.toBearer() = BearerTokens(accessToken, refreshToken)
