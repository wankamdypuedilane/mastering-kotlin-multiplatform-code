// ch09-ktor/cli/src/main/kotlin/hearthseed/cli/Main.kt
package hearthseed.cli

import hearthseed.network.ApiConfig
import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.createHttpClient
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.coroutines.runBlocking

// Calls a running hearthseed-server (code/hearthseed-server: ./gradlew run).
fun main(args: Array<String>) = runBlocking {
    val config = ApiConfig(
        logLevel = if ("--log" in args) LogLevel.HEADERS else LogLevel.NONE,
    )
    createHttpClient(config).use { client ->
        println("engine: ${client.engine::class.simpleName}")
        val api = HearthseedApi(client, memberToken = "demo-token-ada")

        api.health()
            .show("health") { "${it.status}, ${it.varieties} varieties" }
        api.varieties(query = "tomato", pageSize = 2).show("tomatoes") { page ->
            "${page.total} found: " + page.items.joinToString { it.commonName }
        }
        api.variety("var-999").show("var-999") { it.commonName }
        api.lots("var-004").show("lots of var-004") { lots ->
            lots.joinToString { "${it.id} ${it.status}" }
        }
        repeat(2) {
            api.borrow(lotId = "lot-006", memberId = "mem-001")
                .show("borrow lot-006") { "${it.id} on ${it.borrowedOn}" }
        }
        HearthseedApi(client, memberToken = "YOUR_MEMBER_TOKEN")
            .loansOf("mem-001").show("loans, wrong token") { "${it.size}" }
        api.loansOf("mem-001").show("loans of mem-001") { loans ->
            loans.joinToString { it.lotId }
        }
    }
}

private fun <T> ApiResult<T>.show(label: String, format: (T) -> String) =
    when (this) {
        is ApiResult.Success -> println("$label: ${format(value)}")
        is ApiResult.Failure -> println("$label -> $error")
    }
