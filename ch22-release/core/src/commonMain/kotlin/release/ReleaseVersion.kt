// ch22-release/core/src/commonMain/kotlin/release/ReleaseVersion.kt
package release

/**
 * One MAJOR.MINOR.PATCH version for every store. Each store adds its own
 * limits, so the checks live next to the type and are unit-tested.
 */
data class ReleaseVersion(val major: Int, val minor: Int, val patch: Int) {
    init {
        require(major >= 0 && minor >= 0 && patch >= 0) {
            "version parts must not be negative: $major.$minor.$patch"
        }
    }

    override fun toString(): String = "$major.$minor.$patch"

    /** Windows MSI/EXE: MAJOR.MINOR.BUILD, at most 255.255.65535. */
    val fitsMsi: Boolean
        get() = major <= 255 && minor <= 255 && patch <= 65535

    companion object {
        private val format = Regex("""(\d{1,9})\.(\d{1,9})\.(\d{1,9})""")

        /** Parses "1.4.2", the form of Apple's CFBundleShortVersionString. */
        fun parse(text: String): ReleaseVersion {
            val match = requireNotNull(format.matchEntire(text.trim())) {
                "expected MAJOR.MINOR.PATCH, got '$text'"
            }
            val (major, minor, patch) = match.destructured
            return ReleaseVersion(major.toInt(), minor.toInt(), patch.toInt())
        }
    }
}

/** The greatest versionCode Google Play accepts. */
const val MAX_VERSION_CODE: Int = 2_100_000_000

/** Validates a build number used as Android versionCode or CFBundleVersion. */
fun checkBuildNumber(build: Int): Int {
    require(build in 1..MAX_VERSION_CODE) {
        "build number must be in 1..$MAX_VERSION_CODE, got $build"
    }
    return build
}
