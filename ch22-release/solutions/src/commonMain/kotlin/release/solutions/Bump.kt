// ch22-release/solutions/src/commonMain/kotlin/release/solutions/Bump.kt
package release.solutions

import release.ReleaseVersion

enum class Bump { MAJOR, MINOR, PATCH }

/** Semantic-versioning bump: the parts after the bumped one reset to zero. */
fun ReleaseVersion.bump(kind: Bump): ReleaseVersion = when (kind) {
    Bump.MAJOR -> ReleaseVersion(major + 1, 0, 0)
    Bump.MINOR -> ReleaseVersion(major, minor + 1, 0)
    Bump.PATCH -> ReleaseVersion(major, minor, patch + 1)
}
