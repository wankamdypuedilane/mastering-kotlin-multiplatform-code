package hearthseed.labels.solutions

import hearthseed.labels.coreModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SolutionsVerifyTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun detailModuleDeclaresItsParameter() {
        module { includes(coreModule, detailModule) }.verify(
            injections = injectedParameters(
                definition<LabelDetailViewModel>(String::class),
            ),
        )
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun queueModuleNeedsASinkFromOutside() {
        // Each app provides the sink at startup: declare it as an extra type.
        queueModule.verify(extraTypes = listOf(LabelSink::class))
        // Without it, verify() reports the missing definition.
        assertFailsWith<MissingKoinDefinitionException> { queueModule.verify() }
    }

}
