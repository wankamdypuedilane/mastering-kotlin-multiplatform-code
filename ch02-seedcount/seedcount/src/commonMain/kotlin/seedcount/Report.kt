package seedcount

fun PlanResult.render(): String = when (this) {
    is PlanResult.Ready -> lines.joinToString("\n") { line ->
        with(line) {
            val status = if (shortfall == 0) "ok" else "buy $shortfall"
            "$variety: sow $seedsToSow for $plants plants, " +
                "$packetsNeeded/$packetsInStock packets, $status"
        }
    }
    is PlanResult.Rejected -> errors.joinToString("\n") { it.message() }
}

fun PlanError.message(): String = when (this) {
    is PlanError.UnknownVariety -> "No packets of '$variety' in stock"
    is PlanError.InvalidPlantCount ->
        "$variety: plant count must be positive, was $plants"
}
