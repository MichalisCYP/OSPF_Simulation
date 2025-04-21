package Model;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {

    private final Map<String, RouteEntry> entries = new HashMap<>();

    public void update(String destId, RouteEntry entry) {
        entries.put(destId, entry);
    }

    public RouteEntry getEntry(String destId) {
        return entries.get(destId);
    }

    public Map<String, RouteEntry> getEntries() {
        return entries;
    }

}