// hearthseed/shared/src/jvmTest/kotlin/hearthseed/shared/AppModulesTest.kt
package hearthseed.shared

import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.LoanRepository
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.loans.BorrowViewModel
import hearthseed.feature.loans.MyLoansViewModel
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.defaultBaseUrl
import hearthseed.shared.di.hearthseedModules
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
        koin.get<LoanRepository>()
        koin.get<CatalogViewModel>()
        koin.get<VarietyDetailViewModel> { parametersOf("var-010") }
        koin.get<BorrowViewModel> { parametersOf("lot-019") }
        koin.get<MyLoansViewModel>()
        koin.close() // closes the HttpClient (onClose)
    }

    @Test fun desktopTalksToLocalhost() {
        assertEquals("http://localhost:8085/api/v1/", defaultBaseUrl())
    }
}
