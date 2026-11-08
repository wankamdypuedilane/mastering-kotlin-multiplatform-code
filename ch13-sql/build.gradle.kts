plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
// The SQL scripts run on the JVM through sqlite-jdbc on both runners.
tasks.register("ciLinux") {
    dependsOn(":runner:test")
}
tasks.register("ciMacos") {
    dependsOn(":runner:test")
}
