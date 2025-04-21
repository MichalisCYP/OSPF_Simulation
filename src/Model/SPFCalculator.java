package Model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SPFCalculator {

    private final RoutingTable routingTable;
    private final LSDB lsdb;

    public SPFCalculator(RoutingTable routingTable, LSDB lsdb) {
        this.routingTable = routingTable;
        this.lsdb = lsdb;
    }

    private RoutingTable buildRoutingTable(Map<String, RouteEntry> shortestPaths) {
        RoutingTable newRoutingTable = new RoutingTable();
        for (RouteEntry entry : shortestPaths.values()) {
            newRoutingTable.update(entry.destinationId, entry);
        }
        return newRoutingTable;
    }

    public void calculateShortestPaths(String startRouterId) {
        Map<String, RouteEntry> shortestPaths = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<RouteEntry> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(entry -> entry.cost));

        // Initialize the starting router
        shortestPaths.put(startRouterId, new RouteEntry(startRouterId, null, 0));
        priorityQueue.add(new RouteEntry(startRouterId, null, 0));

        while (!priorityQueue.isEmpty()) {
            RouteEntry currentEntry = priorityQueue.poll();
            String currentRouterId = currentEntry.destinationId;

            if (visited.contains(currentRouterId)) {
                continue; // Skip if already visited
            }
            visited.add(currentRouterId);

            // Get neighbors from the LSDB
            LSA currentLSA = lsdb.getLSA(currentRouterId);
            if (currentLSA != null) {
                for (Map.Entry<String, Integer> link : currentLSA.getLinks().entrySet()) {
                    String neighborId = link.getKey();
                    int linkCost = link.getValue();

                    if (!visited.contains(neighborId)) {
                        int newCost = currentEntry.cost + linkCost;
                        if (!shortestPaths.containsKey(neighborId) || newCost < shortestPaths.get(neighborId).cost) {
                            shortestPaths.put(neighborId, new RouteEntry(neighborId, currentRouterId, newCost));
                            priorityQueue.add(new RouteEntry(neighborId, currentRouterId, newCost));
                        }
                    }
                }
            }
        }

        // Update the routing table
        RoutingTable updatedRoutingTable = buildRoutingTable(shortestPaths);
        routingTable.getEntries().clear();
        routingTable.getEntries().putAll(updatedRoutingTable.getEntries());
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}