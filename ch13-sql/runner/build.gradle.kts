plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.sqlite.jdbc)
    testImplementation(kotlin("test"))
}

val sqlDir = rootProject.layout.projectDirectory

tasks.test {
    useJUnitPlatform()
    // The SQL files live outside the module: declare them as inputs.
    inputs.files(sqlDir.files("schema.sql", "seed.sql"))
    inputs.dir(sqlDir.dir("queries"))
    inputs.dir(sqlDir.dir("exercises"))
    inputs.dir(sqlDir.dir("expected"))
    systemProperty("sql.dir", sqlDir.asFile.absolutePath)
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
    }
}
