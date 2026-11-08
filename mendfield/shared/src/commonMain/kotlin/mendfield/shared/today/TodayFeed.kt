// mendfield/shared/src/commonMain/kotlin/mendfield/shared/today/TodayFeed.kt
package mendfield.shared.today

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.todayIn
import mendfield.core.data.EventRepository
import mendfield.core.data.OutcomeRepository
import mendfield.core.data.SyncCoordinator
import mendfield.core.model.Event
import mendfield.core.model.OutcomeKind
import mendfield.core.ui.hhmm
import mendfield.core.ui.label

/**
 * What the SwiftUI "Today" screen shows: plain values only (strings, ints,
 * a boolean), which cross to Swift without any wrapper.
 */
data class TodaySummary(
    val eventTitle: String,
    val whenText: String,
    val registered: Int,
    val waiting: Int,
    val fixed: Int,
    val repairRate: String,
    val pendingChanges: Int,
    val online: Boolean,
)

/** The next event that has not ended, or the last one; null without events. */
fun pickTodayEvent(events: List<Event>, clock: Clock): Event? =
    events.firstOrNull { it.date >= clock.todayIn(it.timeZone) } ?: events.lastOrNull()

/** Swift keeps this and calls cancel() when the screen goes away. */
class FeedSubscription internal constructor(private val job: Job) {
    fun cancel() = job.cancel()
}

/**
 * A small API for Swift: callbacks instead of Flow (an Objective-C header
 * has no Flow), always delivered on the main thread.
 */
class TodayFeed(
    private val events: EventRepository,
    private val outcomes: OutcomeRepository,
    private val sync: SyncCoordinator,
    private val clock: Clock,
    private val main: CoroutineDispatcher = Dispatchers.Main,
) {
    private val scope = CoroutineScope(SupervisorJob() + main)

    @OptIn(ExperimentalCoroutinesApi::class)
    val summaries: Flow<TodaySummary?> = events.events
        .map { pickTodayEvent(it, clock) }
        .distinctUntilChanged()
        .flatMapLatest { event ->
            if (event == null) flowOf(null)
            else combine(
                events.board(event.id), outcomes.stats(event.id), sync.status,
            ) { board, stats, status ->
                val scheduled = board?.slots?.mapNotNull { it.itemId }?.toSet().orEmpty()
                TodaySummary(
                    eventTitle = event.title,
                    whenText = "${event.date.label()}, " +
                        "${event.opensAt.hhmm()}–${event.closesAt.hhmm()}",
                    registered = stats.registered,
                    waiting = board?.items?.count { it.id !in scheduled } ?: 0,
                    fixed = stats.counts[OutcomeKind.FIXED] ?: 0,
                    repairRate = stats.repairRatePercent?.let { "$it %" } ?: "–",
                    pendingChanges = status.pending,
                    online = status.online,
                )
            }
        }
        .distinctUntilChanged()

    /** Swift: let s = feed.watch { summary in ... } */
    fun watch(onChange: (TodaySummary?) -> Unit): FeedSubscription =
        FeedSubscription(scope.launch { summaries.collect { onChange(it) } })

    /** Swift: feed.refresh() from a pull-to-refresh, fire and forget. */
    fun refresh() {
        scope.launch {
            events.refreshEvents()
            pickTodayEvent(events.events.first(), clock)?.let {
                events.refreshEvent(it.id)
            }
        }
    }

    fun close() = scope.cancel()
}
