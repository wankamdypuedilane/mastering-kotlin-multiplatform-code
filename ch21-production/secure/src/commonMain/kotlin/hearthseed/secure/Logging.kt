// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/Logging.kt
package hearthseed.secure

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

/** Redacts every message, then hands it to the real writer. */
class RedactingLogWriter(private val delegate: LogWriter) : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity): Boolean =
        delegate.isLoggable(tag, severity)

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        delegate.log(severity, Redactor.redact(message), tag, throwable)
    }
}

/** The app's logger: every writer is wrapped, release builds log less. */
fun appLogger(
    release: Boolean,
    writers: List<LogWriter> = listOf(platformLogWriter()),
): Logger = Logger(
    config = StaticConfig(
        minSeverity = if (release) Severity.Warn else Severity.Debug,
        logWriterList = writers.map(::RedactingLogWriter),
    ),
    tag = "Hearthseed",
)
