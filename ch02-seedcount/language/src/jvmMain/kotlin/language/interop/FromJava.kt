package language.interop

// Kotlin calling Java: getters become properties, SAM interfaces take
// lambdas, and Java return types are platform types.
fun ledgerSummary(): String {
    val ledger = LegacyLedger()
    ledger.owner = "Larkmoor" // calls setOwner
    val events = mutableListOf<String>()
    ledger.addListener { variety, packets -> events += "$variety+$packets" }
    ledger.add("Pea", 2)
    ledger.add("Kale", 1)
    ledger.add("Pea", 1)

    // Integer! is a platform type: declare it nullable to stay safe.
    val leeks: Int? = ledger.packetsOf("Leek")
    val peas: Int = ledger.packetsOf("Pea") ?: 0
    val names: List<String> = ledger.varieties // read-only view
    return "${ledger.owner}: $names, peas=$peas, leeks=$leeks, " +
        "empty=${ledger.isEmpty}, events=$events"
}
