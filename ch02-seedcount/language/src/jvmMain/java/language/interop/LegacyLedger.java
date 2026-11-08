package language.interop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A Java class, as an older JVM library would ship it. */
public class LegacyLedger {
    /** A Java functional interface: Kotlin can pass a lambda for it. */
    public interface Listener {
        void onAdded(String variety, int packets);
    }

    private final Map<String, Integer> packets = new LinkedHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();
    private String owner = "nobody";

    public String getOwner() { return owner; }
    public void setOwner(String newOwner) { owner = newOwner; }
    public boolean isEmpty() { return packets.isEmpty(); }

    public void addListener(Listener listener) { listeners.add(listener); }

    public void add(String variety, int count) {
        packets.merge(variety, count, Integer::sum);
        for (Listener l : listeners) l.onAdded(variety, count);
    }

    /** Returns null when the variety is unknown: Java does not say so. */
    public Integer packetsOf(String variety) { return packets.get(variety); }

    /** A mutable Java list. */
    public List<String> getVarieties() {
        return new ArrayList<>(packets.keySet());
    }
}
