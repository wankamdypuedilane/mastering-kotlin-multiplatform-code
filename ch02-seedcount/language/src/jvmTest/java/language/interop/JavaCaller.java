package language.interop;

import java.io.IOException;

/** Java code that calls the Kotlin API of ForJava.kt. */
public final class JavaCaller {
    private JavaCaller() {}

    public static String run() {
        int byDefault = SeedMath.packetsFor(60); // @JvmOverloads
        int byThirty = SeedMath.packetsFor(60, 30);
        SeedBank bank = SeedBank.standard(); // @JvmStatic
        String name = bank.name; // @JvmField
        String loaded;
        try {
            loaded = "loaded " + bank.load("");
        } catch (IOException e) { // legal: @Throws
            loaded = "failed: " + e.getMessage();
        }
        return name + "|" + byDefault + "|" + byThirty + "|"
            + SeedBank.MAX_PACKETS + "|" + loaded;
    }
}
