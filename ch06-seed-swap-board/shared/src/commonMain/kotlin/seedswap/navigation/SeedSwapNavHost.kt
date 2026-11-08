package seedswap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import seedswap.board.detail.OfferDetailRoute
import seedswap.board.list.OfferListRoute
import seedswap.core.SwapRepository
import seedswap.proposal.ProposalFormRoute
import seedswap.proposal.ProposalSentScreen

@Composable
fun SeedSwapNavHost(
    repository: SwapRepository,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Board) {
        composable<Board> {
            OfferListRoute(
                repository,
                onOfferClick = { id ->
                    navController.navigate(OfferDetail(id))
                },
            )
        }
        composable<OfferDetail> { entry ->
            val route = entry.toRoute<OfferDetail>()
            OfferDetailRoute(
                route.offerId, repository,
                onPropose = {
                    navController.navigate(MakeProposal(route.offerId))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<MakeProposal> { entry ->
            val route = entry.toRoute<MakeProposal>()
            ProposalFormRoute(
                route.offerId, repository,
                onSent = { proposalId ->
                    val sent = ProposalSent(route.offerId, proposalId)
                    navController.navigate(sent) {
                        // Pop the form: Back from "sent" returns to the offer.
                        popUpTo<OfferDetail>()
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<ProposalSent> { entry ->
            val route = entry.toRoute<ProposalSent>()
            ProposalSentScreen(
                route.proposalId,
                onViewOffer = { navController.popBackStack() },
                onBackToBoard = {
                    navController.popBackStack(route = Board, inclusive = false)
                },
            )
        }
    }
}
