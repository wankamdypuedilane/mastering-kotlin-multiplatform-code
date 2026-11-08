package hearthseed.platform.desktop

import hearthseed.platform.ExportPlanner
import hearthseed.platform.JvmAppDirectories
import hearthseed.platform.platformInfo

fun main() {
    val info = platformInfo()
    val directories = JvmAppDirectories("Hearthseed")
    println("Running on ${info.summary}")
    println("Data folder: ${directories.data}")

    val plan = ExportPlanner(info, directories).plan("inventory")
    println("Next export: ${plan.path}")
    println(plan.header)
}
