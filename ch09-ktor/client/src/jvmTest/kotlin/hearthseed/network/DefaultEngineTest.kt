// ch09-ktor/client/src/jvmTest/kotlin/hearthseed/network/DefaultEngineTest.kt
package hearthseed.network

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEngineTest {
    @Test fun engineDefaultsPicksOkHttpOnTheJvm() {
        createHttpClient(ApiConfig()).use { client ->
            assertEquals("OkHttpEngine", client.engine::class.simpleName)
        }
    }
}
