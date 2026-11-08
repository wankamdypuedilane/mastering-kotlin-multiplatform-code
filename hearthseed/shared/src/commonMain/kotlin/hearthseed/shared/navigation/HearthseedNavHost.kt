// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/navigation/HearthseedNavHost.kt
package hearthseed.shared.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import hearthseed.core.data.MemberSession
import hearthseed.feature.catalog.detail.VarietyDetailRoute
import hearthseed.feature.catalog.list.CatalogRoute
import hearthseed.feature.catalog.summary
import hearthseed.feature.loans.BorrowRoute
import hearthseed.feature.loans.MyLoansRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The only place that knows both features: they never import each other. */
@Composable
fun HearthseedNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController, startDestination = Catalog) {
        composable<Catalog> {
            CatalogRoute(
                viewModel = koinViewModel(),
                onVarietyClick = { navController.navigate(VarietyDetail(it)) },
                onOpenLoans = { navController.navigate(MyLoans) },
            )
        }
        composable<VarietyDetail> { entry ->
            val route = entry.toRoute<VarietyDetail>()
            VarietyDetailRoute(
                viewModel = koinViewModel { parametersOf(route.varietyId) },
                onBorrow = { lot, variety ->
                    val summary = "${variety.commonName}: ${lot.summary()}"
                    navController.navigate(Borrow(lot.id, summary))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Borrow> { entry ->
            val route = entry.toRoute<Borrow>()
            BorrowRoute(
                viewModel = koinViewModel { parametersOf(route.lotId) },
                lotSummary = route.lotSummary,
                // Back from the loans goes to the variety, not to the form.
                onShowLoans = {
                    navController.navigate(MyLoans) { popUpTo<VarietyDetail>() }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<MyLoans> {
            MyLoansRoute(
                viewModel = koinViewModel(),
                memberName = koinInject<MemberSession>().displayName,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
