// hearthseed/build-logic/src/main/kotlin/hearthseed/buildlogic/ModuleRules.kt
package hearthseed.buildlogic

private val appModules = setOf(":androidApp", ":desktopApp")

/**
 * Exercise 11.4: the dependency rules of Hearthseed, as data. [edges] maps a
 * module to the modules its main (non-test) code depends on.
 */
fun moduleRuleViolations(edges: Map<String, Set<String>>): List<String> =
    edges.toSortedMap().flatMap { (from, targets) ->
        targets.sorted().mapNotNull { to ->
            when {
                from.startsWith(":feature:") && to.startsWith(":feature:") ->
                    "$from -> $to: features never depend on each other"
                from.startsWith(":core:") && !to.startsWith(":core:") ->
                    "$from -> $to: core modules depend only on core modules"
                to == ":shared" && from !in appModules ->
                    "$from -> $to: only the app modules use :shared"
                to == ":core:testing" ->
                    "$from -> $to: test doubles belong in test source sets"
                else -> null
            }
        }
    }
