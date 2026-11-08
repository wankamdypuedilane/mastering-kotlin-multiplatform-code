// mendfield/shared/src/jvmTest/kotlin/mendfield/shared/AppModulesTest.kt
package mendfield.shared

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mendfield.core.data.CafeCache
import mendfield.core.data.ConnectivityMonitor
import mendfield.core.data.EventRepository
import mendfield.core.data.ProbingConnectivityMonitor
import mendfield.core.data.SyncCoordinator
import mendfield.core.database.RoomCafeCache
import mendfield.core.database.RoomOutboxStore
import mendfield.core.database.mendfieldDatabase
import mendfield.core.sync.OutboxStore
import mendfield.feature.events.EventBoardViewModel
import mendfield.feature.events.EventListViewModel
import mendfield.feature.intake.IntakeViewModel
import mendfield.feature.outcomes.EventStatsViewModel
import mendfield.feature.outcomes.LogOutcomeViewModel
import mendfield.shared.di.AppConfig
import mendfield.shared.di.defaultBaseUrl
import mendfield.shared.di.inMemoryStorageModule
import mendfield.shared.di.mendfieldModules
import mendfield.shared.sync.SyncStatusViewModel
import mendfield.shared.today.TodayFeed
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun theWholeGraphResolves() {
        // The real network module (HttpClient included), no request made.
        val koin = koinApplication {
            modules(mendfieldModules(AppConfig(), storage = inMemoryStorageModule))
        }.koin
        assertSame(koin.get<EventRepository>(), koin.get<EventRepository>())
        assertIs<ProbingConnectivityMonitor>(koin.get<ConnectivityMonitor>())
        koin.get<SyncCoordinator>()
        koin.get<TodayFeed>()
        koin.get<EventListViewModel>()
        koin.get<EventBoardViewModel> { parametersOf("evt-02") }
        koin.get<IntakeViewModel> { parametersOf("evt-02") }
        koin.get<LogOutcomeViewModel> { parametersOf("item-001") }
        koin.get<EventStatsViewModel> { parametersOf("evt-02") }
        koin.get<SyncStatusViewModel>()
        koin.close() // closes the HttpClient and cancels the app scope
    }

    @Test fun theRoomStorageOfTheDesktopResolves() = runTest {
        val file = kotlin.io.path.createTempFile("mendfield", ".db").toFile()
        file.delete() // Room creates it
        val room = module {
            single { mendfieldDatabase(file) }
            single<CafeCache> { RoomCafeCache(get()) }
            single<OutboxStore> { RoomOutboxStore(get()) }
        }
        val koin = koinApplication { modules(mendfieldModules(storage = room)) }.koin
        assertIs<RoomCafeCache>(koin.get<CafeCache>())
        assertEquals(emptyList(), koin.get<EventRepository>().events.first())
        koin.close()
        file.delete()
    }

    @Test fun desktopTalksToLocalhost() {
        assertEquals("http://localhost:8086/api/v1/", defaultBaseUrl())
    }
}
