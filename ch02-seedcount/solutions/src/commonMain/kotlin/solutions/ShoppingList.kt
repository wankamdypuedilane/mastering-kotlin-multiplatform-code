package solutions

import seedcount.PlanResult

// Exercise 2.2: what to buy, largest shortfall first.
fun PlanResult.Ready.shoppingList(): Map<String, Int> =
    lines.filter { it.shortfall > 0 }
        .sortedByDescending { it.shortfall }
        .associate { it.variety to it.shortfall }
