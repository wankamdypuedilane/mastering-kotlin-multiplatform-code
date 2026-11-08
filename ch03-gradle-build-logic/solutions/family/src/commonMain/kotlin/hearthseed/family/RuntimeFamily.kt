// ch03-gradle-build-logic/solutions/family/src/commonMain/.../RuntimeFamily.kt
package hearthseed.family

/** "jvm", "web" or "ios": one actual per group of the hierarchy. */
expect fun runtimeFamily(): String

fun greeting(): String = "Hearthseed running on ${runtimeFamily()}"
