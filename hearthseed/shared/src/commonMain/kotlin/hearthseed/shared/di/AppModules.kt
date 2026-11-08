// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/di/AppModules.kt
package hearthseed.shared.di

import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DefaultCatalogRepository
import hearthseed.core.data.DefaultLoanRepository
import hearthseed.core.data.LoanRepository
import hearthseed.core.data.MemberSession
import hearthseed.core.data.OfflineFallbackCatalogRepository
import hearthseed.core.data.local.InMemoryVarietyLocalSource
import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.network.ApiConfig
import hearthseed.core.network.HearthseedApi
import hearthseed.core.network.createHttpClient
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.loans.BorrowViewModel
import hearthseed.feature.loans.MyLoansViewModel
import io.ktor.client.plugins.logging.LogLevel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Everything that differs between runs of the app. */
data class AppConfig(
    /** Must end with "/"; see [defaultBaseUrl] for the per-platform value. */
    val baseUrl: String = defaultBaseUrl(),
    val session: MemberSession = MemberSession.Demo,
    val logLevel: LogLevel = LogLevel.NONE,
)

fun networkModule(config: AppConfig) = module {
    single {
        createHttpClient(ApiConfig(config.baseUrl, logLevel = config.logLevel))
    }.withOptions { onClose { it?.close() } } // when Koin stops
    single { HearthseedApi(get(), memberToken = config.session.token) }
}

/** The seam of Chapter 14; tests and the web app keep this in-memory one. */
val inMemoryLocalModule = module {
    single<VarietyLocalSource> { InMemoryVarietyLocalSource() }
}

fun dataModule(config: AppConfig) = module {
    single { config.session }
    single<CatalogRepository> {
        // Offline, pages come from the varieties stored in the local source.
        OfflineFallbackCatalogRepository(
            delegate = DefaultCatalogRepository(api = get(), local = get()),
            local = get(),
        )
    }
    single<LoanRepository> {
        DefaultLoanRepository(api = get(), local = get(), catalog = get(),
            session = get())
    }
}

val viewModelModule = module {
    viewModel { CatalogViewModel(get()) }
    viewModel { (id: String) -> VarietyDetailViewModel(id, get()) }
    viewModel { (lotId: String) -> BorrowViewModel(lotId, get()) }
    viewModel { MyLoansViewModel(get()) }
}

fun hearthseedModules(
    config: AppConfig = AppConfig(),
    localModule: Module = inMemoryLocalModule,
): List<Module> = listOf(
    networkModule(config), localModule, dataModule(config), viewModelModule,
)

/** The one entry point of every platform (Android, iOS, desktop, web). */
fun initKoin(
    config: AppConfig = AppConfig(),
    extra: KoinAppDeclaration? = null,
) {
    startKoin {
        extra?.invoke(this)
        modules(hearthseedModules(config, platformLocalModule())) // Room
    }
}
