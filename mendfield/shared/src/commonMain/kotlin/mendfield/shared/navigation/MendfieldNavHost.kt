// mendfield/shared/src/commonMain/kotlin/mendfield/shared/navigation/MendfieldNavHost.kt
package mendfield.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import mendfield.feature.events.BoardNavigation
import mendfield.feature.events.EventBoardRoute
import mendfield.feature.events.EventListRoute
import mendfield.feature.intake.IntakeRoute
import mendfield.feature.outcomes.EventStatsRoute
import mendfield.feature.outcomes.LogOutcomeRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The only place that knows every feature: they never import each other. */
@Composable
fun MendfieldNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController, startDestination = Events, modifier = modifier) {
        composable<Events> {
            EventListRoute(koinViewModel(), onOpenEvent = { navController.navigate(Board(it)) })
        }
        composable<Board> { entry ->
            val eventId = entry.toRoute<Board>().eventId
            EventBoardRoute(
                viewModel = koinViewModel { parametersOf(eventId) },
                navigation = BoardNavigation(
                    onBack = { navController.popBackStack() },
                    onRegisterItem = { navController.navigate(Intake(eventId)) },
                    onOpenItem = { navController.navigate(LogOutcome(it)) },
                    onOpenStats = { navController.navigate(Stats(eventId)) },
                ),
            )
        }
        composable<Intake> { entry ->
            val eventId = entry.toRoute<Intake>().eventId
            IntakeRoute(
                viewModel = koinViewModel { parametersOf(eventId) },
                // Back to the board, where the new item waits for a slot.
                onRegistered = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<LogOutcome> { entry ->
            val itemId = entry.toRoute<LogOutcome>().itemId
            LogOutcomeRoute(koinViewModel { parametersOf(itemId) },
                onDone = { navController.popBackStack() })
        }
        composable<Stats> { entry ->
            val eventId = entry.toRoute<Stats>().eventId
            EventStatsRoute(koinViewModel { parametersOf(eventId) },
                onBack = { navController.popBackStack() })
        }
    }
}
