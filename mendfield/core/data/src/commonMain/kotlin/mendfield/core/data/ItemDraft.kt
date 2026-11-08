// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/ItemDraft.kt
package mendfield.core.data

/** The server's limits (server/README.md), checked before anything is queued. */
object ItemLimits {
    const val DESCRIPTION = 200
    const val VISITOR_NAME = 80
    const val NOTE = 500
}

enum class DraftProblem { DESCRIPTION_EMPTY, DESCRIPTION_TOO_LONG,
    VISITOR_EMPTY, VISITOR_TOO_LONG }

/** Empty when the form may be saved; the same rules as the server. */
fun draftProblems(description: String, visitorName: String): Set<DraftProblem> =
    buildSet {
        val d = description.trim()
        val v = visitorName.trim()
        if (d.isEmpty()) add(DraftProblem.DESCRIPTION_EMPTY)
        if (d.length > ItemLimits.DESCRIPTION) add(DraftProblem.DESCRIPTION_TOO_LONG)
        if (v.isEmpty()) add(DraftProblem.VISITOR_EMPTY)
        if (v.length > ItemLimits.VISITOR_NAME) add(DraftProblem.VISITOR_TOO_LONG)
    }

/** The result of a local write: queued for the server, or refused here. */
sealed interface WriteResult<out T> {
    data class Saved<T>(val value: T) : WriteResult<T>
    data class Refused(val reason: String) : WriteResult<Nothing>
}
