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

    @Test fun theAppGraphOfPartTwoIsValid() {
        val edges = mapOf(
            ":core:data" to setOf(":core:domain", ":core:network", ":core:sync"),
            ":core:database" to setOf(":core:data", ":core:sync"),
            ":core:testing" to setOf(":core:data"),
            ":feature:events" to setOf(":core:data"),
            ":shared" to setOf(":core:data", ":core:database", ":feature:events"),
            ":androidApp" to setOf(":shared"),
            ":desktopApp" to setOf(":shared"),
            ":webApp" to setOf(":shared", ":core:data"),
        )
        assertEquals(emptyList(), moduleRuleViolations(edges))
    }

    @Test fun featuresAndCoreKeepTheirDistance() {
        val edges = mapOf(
            ":feature:events" to setOf(":core:database", ":shared"),
            ":core:data" to setOf(":core:testing"),
        )
        assertEquals(
            listOf(
                ":core:data -> :core:testing: test doubles are for test source sets only",
                ":feature:events -> :core:database: features read data through :core:data",
                ":feature:events -> :shared: only the app modules use the app shell",
            ),
            moduleRuleViolations(edges),
        )
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
