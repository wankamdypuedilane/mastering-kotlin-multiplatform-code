package hearthseed.labels

import org.koin.test.verify.verify
import org.koin.core.annotation.KoinExperimentalAPI
import kotlin.test.Test

class CoreModuleVerifyTest {
    // verify() inspects constructors with reflection: JVM only.
    @OptIn(KoinExperimentalAPI::class) // see the Version note in the chapter
    @Test
    fun coreModuleIsComplete() {
        coreModule.verify()
    }
}
