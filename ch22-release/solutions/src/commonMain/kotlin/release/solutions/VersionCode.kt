// ch22-release/solutions/src/commonMain/kotlin/release/solutions/VersionCode.kt
package release.solutions

import release.ReleaseVersion
import release.checkBuildNumber

/**
 * Encodes MAJOR.MINOR.PATCH plus a per-version build counter as
 * MMMM mm pp bb (major up to 2099, the others up to 99), so a newer
 * version always gets a greater versionCode and stays below Play's limit.
 */
fun androidVersionCode(version: ReleaseVersion, build: Int): Int {
    require(version.major <= 2099) { "major ${version.major} > 2099" }
    require(version.minor <= 99 && version.patch <= 99) {
        "minor/patch > 99 in $version"
    }
    require(build in 0..99) { "build $build not in 0..99" }
    val code = version.major * 1_000_000 + version.minor * 10_000 +
        version.patch * 100 + build
    return checkBuildNumber(code)
}
