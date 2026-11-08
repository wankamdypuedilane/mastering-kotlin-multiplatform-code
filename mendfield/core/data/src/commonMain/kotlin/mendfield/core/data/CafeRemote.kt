// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/CafeRemote.kt
package mendfield.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.network.ApiResult
import mendfield.core.network.MendfieldApi

/** What the repositories read from the server (writes go through the outbox). */
interface CafeRemote {
    suspend fun events(): ApiResult<List<Event>>
    suspend fun fixers(): ApiResult<List<Fixer>>
    suspend fun eventData(eventId: String): ApiResult<EventData>

    /** True when the server answers: the question the outbox really asks. */
    suspend fun isReachable(): Boolean
}

class ApiCafeRemote(private val api: MendfieldApi) : CafeRemote {
    override suspend fun events() = api.events()
    override suspend fun fixers() = api.fixers()

    /** Three requests in parallel; the first failure is the result. */
    override suspend fun eventData(eventId: String): ApiResult<EventData> =
        coroutineScope {
            val slots = async { api.slots(eventId) }
            val items = async { api.items(eventId) }
            val outcomes = async { api.outcomes(eventId) }
            val s = slots.await()
            val i = items.await()
            val o = outcomes.await()
            when {
                s is ApiResult.Failure -> s
                i is ApiResult.Failure -> i
                o is ApiResult.Failure -> o
                else -> ApiResult.Success(EventData(
                    (s as ApiResult.Success).value,
                    (i as ApiResult.Success).value,
                    (o as ApiResult.Success).value,
                ))
            }
        }

    override suspend fun isReachable(): Boolean =
        api.health() is ApiResult.Success
}
