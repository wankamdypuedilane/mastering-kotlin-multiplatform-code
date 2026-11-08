// ch03-gradle-build-logic/solutions/packets/src/commonMain/.../Packets.kt
package hearthseed.packets

import hearthseed.units.Grams

/** Number of packets of [packetSize] needed to hold [total], rounded up. */
fun packetsFor(total: Grams, packetSize: Grams): Int {
    require(packetSize.value > 0) { "packetSize must be > 0" }
    return (total.value + packetSize.value - 1) / packetSize.value
}
