package Model;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    // This class represents the routing table of the OSPF router
    // It contains a map of destination IDs to RouteEntry objects
    // Each RouteEntry contains the destination ID, next hop, and cost to reach the destination
    // The routing table is built using the LSDB and Dijkstra's algorithm

    private final Map<String, RouteEntry> entries = new HashMap<>(); // Map of destination ID to RouteEntry

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