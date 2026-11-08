package hearthseed.labels.ui

import org.koin.dsl.module
import org.koin.test.verify.verify
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.test.Test

class AppModulesVerifyTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun theWholeGraphCanBeBuilt() {
        // verify() checks one module and what it includes, so wrap the list.
        module { includes(appModules()) }.verify()
    }
}
