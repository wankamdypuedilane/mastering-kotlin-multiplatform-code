// hearthseed/shared/src/jvmTest/kotlin/hearthseed/shared/AppModulesTest.kt
package hearthseed.shared

import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.LoanRepository
import hearthseed.core.data.OfflineFallbackCatalogRepository
import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.database.RoomVarietyLocalSource
import hearthseed.core.database.hearthseedDatabase
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.loans.BorrowViewModel
import hearthseed.feature.loans.MyLoansViewModel
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.defaultBaseUrl
import hearthseed.shared.di.hearthseedModules
import hearthseed.shared.di.roomLocalModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun theWholeGraphResolves() {
        // Builds the real graph (HttpClient included) without any request.
        val koin = koinApplication { modules(hearthseedModules(AppConfig())) }.koin
        assertSame(koin.get<CatalogRepository>(), koin.get<CatalogRepository>())
        // The catalog falls back to the local source when offline.
        assertIs<OfflineFallbackCatalogRepository>(koin.get<CatalogRepository>())
        koin.get<LoanRepository>()
        koin.get<CatalogViewModel>()
        koin.get<VarietyDetailViewModel> { parametersOf("var-010") }
        koin.get<BorrowViewModel> { parametersOf("lot-019") }
        koin.get<MyLoansViewModel>()
        koin.close() // closes the HttpClient (onClose)
    }

    @Test fun theRoomGraphResolvesAndClosesTheDatabase() = runTest {
        val file = kotlin.io.path.createTempFile("hearthseed", ".db").toFile()
        file.delete() // Room creates it
        val koin = koinApplication {
            modules(hearthseedModules(AppConfig(),
                roomLocalModule { hearthseedDatabase(file) }))
        }.koin
        val local = koin.get<VarietyLocalSource>()
        assertIs<RoomVarietyLocalSource>(local)
        // A repository, not a ViewModel: a ViewModel would start loading, fall
        // back to the database offline and outlive the close below.
        assertIs<OfflineFallbackCatalogRepository>(koin.get<CatalogRepository>())
        assertNull(local.variety("var-001")) // empty and working
        koin.close() // onClose closes the database
        assertFailsWith<IllegalStateException> { local.variety("var-001") }
        file.delete()
    }

    @Test fun desktopTalksToLocalhost() {
        assertEquals("http://localhost:8085/api/v1/", defaultBaseUrl())
    }
}
