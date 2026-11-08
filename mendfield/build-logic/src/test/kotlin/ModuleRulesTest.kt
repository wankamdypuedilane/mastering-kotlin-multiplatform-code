// mendfield/build-logic/src/test/kotlin/ModuleRulesTest.kt
import mendfield.buildlogic.androidNamespace
import mendfield.buildlogic.moduleRuleViolations
import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleRulesTest {
    @Test fun theMendfieldGraphIsValid() {
        val edges = mapOf(
            ":core:model" to emptySet(),
            ":core:domain" to setOf(":core:model"),
            ":core:network" to setOf(":core:model"),
            ":core:sync" to setOf(":core:model", ":core:network"),
            ":server" to setOf(":core:model", ":core:domain"),
            ":feature:queue" to setOf(":core:domain", ":core:sync"),
        )
        assertEquals(emptyList(), moduleRuleViolations(edges))
    }

    @Test fun everyRuleHasAMessage() {
        val edges = mapOf(
            ":core:model" to setOf(":core:domain"),
            ":core:domain" to setOf(":core:network"),
            ":core:sync" to setOf(":feature:queue"),
            ":server" to setOf(":core:sync"),
            ":feature:queue" to setOf(":feature:stats"),
        )
        assertEquals(
            listOf(
                ":core:domain -> :core:network: pure modules never depend on I/O modules",
                ":core:model -> :core:domain: the model depends on no other module",
                ":core:sync -> :feature:queue: core modules depend only on core modules",
                ":feature:queue -> :feature:stats: features never depend on each other",
                ":server -> :core:sync: the server shares only the model and the domain",
            ),
            moduleRuleViolations(edges),
        )
    }

    @Test fun namespacesFollowThePath() {
        assertEquals("mendfield.core.domain", androidNamespace(":core:domain"))
        assertEquals("mendfield.server", androidNamespace(":server"))
    }
}
