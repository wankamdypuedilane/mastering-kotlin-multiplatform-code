// hearthseed/webApp/support/src/webMain/kotlin/hearthseed/web/support/solutions/BrowserChoices.kt
package hearthseed.web.support.solutions

import hearthseed.web.support.BrowserStorage
import hearthseed.web.support.InMemoryStorage
import hearthseed.web.support.KeyValueStorage
import hearthseed.web.support.localStorageOrMemory
import kotlinx.browser.window

// Exercise 18.2: sessionStorage is cleared when the tab closes.
fun sessionStorageOrMemory(): KeyValueStorage = try {
    BrowserStorage(window.sessionStorage)
} catch (e: Throwable) {
    InMemoryStorage()
}

/** The storage that the address asks for (Exercise 18.2). */
fun storageForPage(): KeyValueStorage =
    when (storageChoice(window.location.search)) {
        StorageChoice.LOCAL -> localStorageOrMemory()
        StorageChoice.SESSION -> sessionStorageOrMemory()
    }
