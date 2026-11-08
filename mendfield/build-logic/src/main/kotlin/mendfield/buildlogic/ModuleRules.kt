// mendfield/build-logic/src/main/kotlin/mendfield/buildlogic/ModuleRules.kt
package mendfield.buildlogic

/** Modules that may depend on nothing but the standard library and datetime. */
private val leafModules = setOf(":core:model")

/** Pure logic: no I/O modules below it. */
private val pureModules = setOf(":core:model", ":core:domain")

/**
 * The dependency rules of Mendfield, as data. [edges] maps a module to the
 * modules its main (non-test) code depends on.
 */
fun moduleRuleViolations(edges: Map<String, Set<String>>): List<String> =
    edges.toSortedMap().flatMap { (from, targets) ->
        targets.sorted().mapNotNull { to -> violation(from, to) }
    }

private fun violation(from: String, to: String): String? = when {
    from in leafModules ->
        "$from -> $to: the model depends on no other module"
    from in pureModules && to !in pureModules ->
        "$from -> $to: pure modules never depend on I/O modules"
    from.startsWith(":core:") && !to.startsWith(":core:") ->
        "$from -> $to: core modules depend only on core modules"
    from == ":server" && to !in pureModules ->
        "$from -> $to: the server shares only the model and the domain"
    from.startsWith(":feature:") && to.startsWith(":feature:") ->
        "$from -> $to: features never depend on each other"
    else -> null
}
