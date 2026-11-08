// hearthseed/solutions/src/jvmTest/kotlin/hearthseed/solutions/LoadMoreUiTest.kt
package hearthseed.solutions

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LoadMoreUiTest {
    @Test fun scrollingNearTheEndLoadsOnce() = runComposeUiTest {
        var calls = 0
        setContent {
            val state = rememberLazyListState()
            LazyColumn(Modifier.height(200.dp).testTag("list"), state = state) {
                items(30) { Text("Row $it", Modifier.height(40.dp)) }
            }
            LoadMoreWhenNearEnd(state, canLoadMore = true, onLoadMore = { calls++ })
        }
        waitForIdle()
        assertEquals(0, calls)
        onNodeWithTag("list").performScrollToIndex(27)
        waitForIdle()
        onNodeWithTag("list").performScrollToIndex(29) // still near the end
        waitForIdle()
        assertEquals(1, calls)
    }
}
