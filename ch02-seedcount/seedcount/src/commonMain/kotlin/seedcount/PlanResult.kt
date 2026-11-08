package seedcount

sealed interface PlanError {
    val variety: String

    data class UnknownVariety(override val variety: String) : PlanError
    data class InvalidPlantCount(
        override val variety: String,
        val plants: Int,
    ) : PlanError
}

sealed interface PlanResult {
    data class Ready(val lines: List<PlanLine>) : PlanResult {
        val totalShortfall: Int get() = lines.sumOf { it.shortfall }
    }

    data class Rejected(val errors: List<PlanError>) : PlanResult
}
