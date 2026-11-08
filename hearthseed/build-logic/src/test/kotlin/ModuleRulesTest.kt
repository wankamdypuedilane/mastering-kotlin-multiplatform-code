// hearthseed/build-logic/src/test/kotlin/ModuleRulesTest.kt
import hearthseed.buildlogic.androidNamespace
import hearthseed.buildlogic.moduleRuleViolations
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleRulesTest {
    @Test fun theHearthseedGraphIsValid() {
        val edges = mapOf(
            ":core:data" to setOf(":core:model", ":core:network"),
            ":feature:catalog" to setOf(":core:data"),
            ":shared" to setOf(":core:data", ":feature:catalog", ":feature:loans"),
            ":androidApp" to setOf(":shared"),
        )
        assertEquals(emptyList(), moduleRuleViolations(edges))
    }

    @Test fun everyRuleHasAMessage() {
        val edges = mapOf(
            ":feature:loans" to setOf(":feature:catalog", ":core:testing"),
            ":core:data" to setOf(":shared"),
        )
        assertEquals(
            listOf(
                ":core:data -> :shared: core modules depend only on core modules",
                ":feature:loans -> :core:testing: test doubles belong in test source sets",
                ":feature:loans -> :feature:catalog: features never depend on each other",
            ),
            moduleRuleViolations(edges),
        )
    }

    @Test fun namespacesFollowThePath() {
        assertEquals("hearthseed.core.data", androidNamespace(":core:data"))
        assertEquals("hearthseed.androidApp", androidNamespace(":androidApp"))
    }
}
