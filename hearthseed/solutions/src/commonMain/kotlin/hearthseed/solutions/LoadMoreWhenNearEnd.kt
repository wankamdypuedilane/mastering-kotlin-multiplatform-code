// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/LoadMoreWhenNearEnd.kt
package hearthseed.solutions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Exercise 12.3: true when the last visible row is close to the end. */
fun shouldLoadMore(
    lastVisibleIndex: Int?,
    itemCount: Int,
    threshold: Int = 3,
): Boolean = lastVisibleIndex != null && itemCount > 0 &&
    lastVisibleIndex >= itemCount - 1 - threshold

/** Calls [onLoadMore] when the user scrolls near the end of [state]'s list. */
@Composable
fun LoadMoreWhenNearEnd(
    state: LazyListState,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    threshold: Int = 3,
) {
    val canLoad by rememberUpdatedState(canLoadMore)
    val load by rememberUpdatedState(onLoadMore)
    LaunchedEffect(state) {
        snapshotFlow {
            val info = state.layoutInfo
            shouldLoadMore(info.visibleItemsInfo.lastOrNull()?.index,
                info.totalItemsCount, threshold)
        }
            .distinctUntilChanged() // once per arrival near the end
            .filter { it && canLoad }
            .collect { load() }
    }
}
