// hearthseed/shared/src/iosMain/kotlin/hearthseed/shared/bridge/Observers.kt
package hearthseed.shared.bridge

import hearthseed.core.data.LoanRepository
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.loans.MyLoansViewModel
import org.koin.mp.KoinPlatform

// Swift: ObserversKt.catalogObserver(), once doInitKoinIos() has run.
// Each call creates a ViewModel; the Swift model that owns it calls close().

fun catalogObserver(): CatalogObserver =
    CatalogObserver { CatalogViewModel(koinGet()) }

fun varietyDetailObserver(varietyId: String): VarietyDetailObserver =
    VarietyDetailObserver { VarietyDetailViewModel(varietyId, koinGet()) }

/** Exercise 16.3. */
fun myLoansObserver(): MyLoansObserver =
    MyLoansObserver { MyLoansViewModel(koinGet<LoanRepository>()) }

private inline fun <reified T : Any> koinGet(): T = KoinPlatform.getKoin().get()
