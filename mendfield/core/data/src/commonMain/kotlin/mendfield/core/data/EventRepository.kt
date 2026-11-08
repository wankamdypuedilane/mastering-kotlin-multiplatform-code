// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/EventRepository.kt
package mendfield.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.network.ApiResult

/**
 * Reads come from the cache only; refreshes copy the server into it. A
 * failed refresh changes nothing on screen: the cached board stays.
 */
class EventRepository(
    private val remote: CafeRemote,
    private val cache: CafeCache,
    private val writer: LocalWriter,
    /** Where rows become models; tests pass their own dispatcher. */
    private val compute: CoroutineDispatcher = Dispatchers.Default,
) {
    val events: Flow<List<Event>> = cache.events()
    val fixers: Flow<List<Fixer>> = cache.fixers()

    /** Null while the event is unknown on this device. */
    fun board(eventId: String): Flow<EventBoard?> = combine(
        cache.events().map { all -> all.firstOrNull { it.id == eventId } }
            .distinctUntilChanged(),
        cache.fixers(),
        cache.eventData(eventId),
    ) { event, fixers, data -> event?.let { EventBoard(it, fixers, data) } }
        .flowOn(compute) // combining lists is not the main thread's job

    suspend fun refreshEvents(): DataResult<Unit> {
        val events = remote.events()
        if (events is ApiResult.Failure) return events.toFailure()
        val fixers = remote.fixers()
        if (fixers is ApiResult.Failure) return fixers.toFailure()
        cache.replaceEvents((events as ApiResult.Success).value)
        cache.replaceFixers((fixers as ApiResult.Success).value)
        return DataResult.Success(Unit)
    }

    suspend fun refreshEvent(eventId: String): DataResult<Unit> =
        when (val result = remote.eventData(eventId)) {
            is ApiResult.Failure -> result.toFailure()
            is ApiResult.Success -> {
                writer.replaceEventData(eventId, result.value)
                DataResult.Success(Unit)
            }
        }
}

private fun ApiResult.Failure.toFailure() = DataResult.Failure(error.toDataError())
