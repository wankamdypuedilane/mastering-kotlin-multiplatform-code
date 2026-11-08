// ch21-production/solutions/src/commonMain/kotlin/hearthseed/solutions/EmailRedaction.kt
package hearthseed.solutions

import hearthseed.secure.Redactor

private val email =
    Regex("""([\w.%+-])[\w.%+-]*(@[A-Za-z0-9.-]+\.[A-Za-z]{2,})""")

/** Exercise 21.1: credentials first, then e-mail addresses (c***@host). */
fun redactForLogs(text: String): String =
    email.replace(Redactor.redact(text), "$1***$2")
