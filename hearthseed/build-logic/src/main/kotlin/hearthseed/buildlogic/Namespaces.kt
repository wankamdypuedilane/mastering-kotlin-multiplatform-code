// hearthseed/build-logic/src/main/kotlin/hearthseed/buildlogic/Namespaces.kt
package hearthseed.buildlogic

/** ":core:data" -> "hearthseed.core.data": one rule for every namespace. */
fun androidNamespace(projectPath: String): String =
    "hearthseed" + projectPath.replace(':', '.').replace('-', '_')
