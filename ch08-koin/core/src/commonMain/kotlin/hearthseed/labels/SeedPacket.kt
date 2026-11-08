package hearthseed.labels

/** One packet of seed, cut from a lot of the Larkmoor Commons Seed Library. */
data class SeedPacket(
    val lotId: String,
    val variety: String,
    val harvestYear: Int,
    val grams: Int,
)

/** Where packets come from. The app only knows this interface. */
interface PacketRepository {
    fun all(): List<SeedPacket>
    fun byLot(lotId: String): SeedPacket?
}

/** A fake repository over a fixed list: no network, no database. */
class InMemoryPacketRepository(
    private val packets: List<SeedPacket>,
) : PacketRepository {
    override fun all(): List<SeedPacket> = packets
    override fun byLot(lotId: String): SeedPacket? =
        packets.firstOrNull { it.lotId == lotId }
}

/** The demo data set used by the apps until a real data layer exists. */
class DemoPacketRepository :
    PacketRepository by InMemoryPacketRepository(demoPackets)

val demoPackets = listOf(
    SeedPacket("L-042", "Moon and Stars watermelon", 2025, 12),
    SeedPacket("L-107", "Cherokee Trail of Tears bean", 2024, 30),
    SeedPacket("L-118", "Golden Bantam sweet corn", 2025, 25),
)
