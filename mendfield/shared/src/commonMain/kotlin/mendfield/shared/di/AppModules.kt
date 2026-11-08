// mendfield/shared/src/commonMain/kotlin/mendfield/shared/di/AppModules.kt
package mendfield.shared.di

import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import mendfield.core.data.ApiCafeRemote
import mendfield.core.data.CafeRemote
import mendfield.core.data.ConnectivityMonitor
import mendfield.core.data.EventRepository
import mendfield.core.data.IntakeRepository
import mendfield.core.data.LocalWriter
import mendfield.core.data.OutcomeRepository
import mendfield.core.data.ProbingConnectivityMonitor
import mendfield.core.data.SyncCoordinator
import mendfield.core.network.ApiConfig
import mendfield.core.network.MendfieldApi
import mendfield.core.network.createHttpClient
import mendfield.core.sync.ApiSyncTransport
import mendfield.core.sync.SyncEngine
import mendfield.core.sync.SyncTransport
import mendfield.feature.events.EventBoardViewModel
import mendfield.feature.events.EventListViewModel
import mendfield.feature.intake.IntakeViewModel
import mendfield.feature.outcomes.EventStatsViewModel
import mendfield.feature.outcomes.LogOutcomeViewModel
import mendfield.shared.sync.SyncStatusViewModel
import mendfield.shared.today.TodayFeed
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * A demo value that the local server accepts (any "demo-organizer-..."):
 * not a secret. A real deployment signs the organizer in instead.
 */
const val DEMO_ORGANIZER_TOKEN = "demo-organizer-brindle"

/** Everything that differs between runs of the app. */
data class AppConfig(
    /** Must end with "/"; see [defaultBaseUrl] for the per-platform value. */
    val baseUrl: String = defaultBaseUrl(),
    val organizerToken: String = DEMO_ORGANIZER_TOKEN,
    val logLevel: LogLevel = LogLevel.NONE,
)

/** The scope of work that outlives every screen: the sync loop. */
class AppScope : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** The server side of the graph; tests replace this whole module. */
fun networkModule(config: AppConfig) = module {
    single {
        createHttpClient(ApiConfig(config.baseUrl, logLevel = config.logLevel))
    }.withOptions { onClose { it?.close() } } // when Koin stops
    single { MendfieldApi(get(), config.organizerToken) }
    single<CafeRemote> { ApiCafeRemote(get()) }
    single<SyncTransport> { ApiSyncTransport(get()) }
    single<ConnectivityMonitor> {
        val remote = get<CafeRemote>()
        ProbingConnectivityMonitor(probe = { remote.isReachable() })
    }
}

/** Repositories and the outbox, over a CafeCache and an OutboxStore. */
fun dataModule(clock: Clock) = module {
    single<Clock> { clock }
    single { AppScope() }.withOptions { onClose { it?.cancel() } }
    single { LocalWriter(get(), get()) }
    single { EventRepository(get(), get(), get()) }
    single { IntakeRepository(get(), get(), get()) }
    single { OutcomeRepository(get(), get(), get()) }
    single { SyncEngine(get(), get()) }
    single { SyncCoordinator(get(), get(), get(), get(), get()) }
    single { TodayFeed(get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { EventListViewModel(get()) }
    viewModel { (eventId: String) ->
        EventBoardViewModel(eventId, get(), get(), get())
    }
    viewModel { (eventId: String) -> IntakeViewModel(eventId, get(), get()) }
    viewModel { (itemId: String) -> LogOutcomeViewModel(itemId, get()) }
    viewModel { (eventId: String) -> EventStatsViewModel(eventId, get(), get()) }
    viewModel { SyncStatusViewModel(get()) }
}

fun mendfieldModules(
    config: AppConfig = AppConfig(),
    storage: Module = platformStorageModule(),
    network: Module = networkModule(config),
    clock: Clock = Clock.System,
): List<Module> = listOf(network, storage, dataModule(clock), viewModelModule)

/** The one entry point of every platform; it also starts the sync loop. */
fun initKoin(
    config: AppConfig = AppConfig(),
    extra: KoinAppDeclaration? = null,
): KoinApplication {
    val app = startKoin {
        extra?.invoke(this)
        modules(mendfieldModules(config))
    }
    app.koin.get<SyncCoordinator>().start(app.koin.get<AppScope>())
    return app
}
