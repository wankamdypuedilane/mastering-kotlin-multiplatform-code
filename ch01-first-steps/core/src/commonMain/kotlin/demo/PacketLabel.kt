package demo

// Exercise 1.2: a pure function in commonMain, shared by every target.
fun packetLabel(variety: String, harvestYear: Int): String {
    require(variety.isNotBlank()) { "variety must not be blank" }
    return "${variety.trim()} - harvest $harvestYear"
}
