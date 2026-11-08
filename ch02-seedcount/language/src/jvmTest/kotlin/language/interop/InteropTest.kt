package language.interop

import kotlin.test.Test
import kotlin.test.assertEquals

class InteropTest {
    @Test
    fun kotlinCallsJava() = assertEquals(
        "Larkmoor: [Pea, Kale], peas=3, leeks=null, empty=false, " +
            "events=[Pea+2, Kale+1, Pea+1]",
        ledgerSummary(),
    )

    @Test
    fun javaCallsKotlin() = assertEquals(
        "Larkmoor Commons|3|2|99|failed: no path given",
        JavaCaller.run(),
    )
}
