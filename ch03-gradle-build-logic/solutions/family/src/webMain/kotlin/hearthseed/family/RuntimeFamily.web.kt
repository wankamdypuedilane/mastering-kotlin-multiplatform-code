// ch03-gradle-build-logic/solutions/family/src/webMain/.../RuntimeFamily.web.kt
package hearthseed.family

// One actual for both js and wasmJs: webMain is their shared parent.
actual fun runtimeFamily(): String = "web"
