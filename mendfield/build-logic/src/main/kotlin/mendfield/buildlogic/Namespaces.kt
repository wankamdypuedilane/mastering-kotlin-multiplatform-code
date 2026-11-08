// mendfield/build-logic/src/main/kotlin/mendfield/buildlogic/Namespaces.kt
package mendfield.buildlogic

/** ":core:domain" -> "mendfield.core.domain": one rule for every namespace. */
fun androidNamespace(projectPath: String): String =
    "mendfield" + projectPath.replace(':', '.').replace('-', '_')
