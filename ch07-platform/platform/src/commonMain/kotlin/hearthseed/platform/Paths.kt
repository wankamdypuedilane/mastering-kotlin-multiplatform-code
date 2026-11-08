package hearthseed.platform

/** The character that separates folders in a path on this platform. */
expect val pathSeparator: Char

/** Joins non-empty [parts] with the platform's [pathSeparator]. */
fun joinPath(vararg parts: String): String =
    parts.filter { it.isNotEmpty() }
        .joinToString(pathSeparator.toString()) { it.trimEnd(pathSeparator) }
