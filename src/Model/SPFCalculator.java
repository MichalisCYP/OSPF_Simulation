package Model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SPFCalculator {
    // This class implements Dijkstra's algorithm to calculate the shortest paths
    // from a source router to all other routers in the network
    // It uses the LSDB (Link State Database) to get the link state advertisements (LSAs)
    // and builds the routing table based on the shortest paths
    // The routing table is a map of destination IDs to RouteEntry objects
    // Each RouteEntry contains the destination ID, next hop, and cost to reach the destination
    
    private final RoutingTable routingTable;
    private final LSDB lsdb;

    public SPFCalculator(RoutingTable routingTable, LSDB lsdb) {
        this.routingTable = routingTable;
        this.lsdb = lsdb;
    }

    private RoutingTable buildRoutingTable(Map<String, RouteEntry> shortestPaths) {
        // This method builds the routing table based on the shortest paths calculated by Dijkstra's algorithm
        // It creates a new RoutingTable object and populates it with the RouteEntry objects from the shortestPaths map
        // The RouteEntry objects contain the destination ID, next hop, and cost to reach the destination
        // The routing table is then returned
        RoutingTable newRoutingTable = new RoutingTable();
        for (RouteEntry entry : shortestPaths.values()) {
            newRoutingTable.update(entry.destinationId, entry);
        }
        return newRoutingTable;
    }

    public void calculateShortestPaths(String sourceRouterId) {
        //https://www.youtube.com/watch?v=NyrHRNiRpds

        //Route Entry has:
        //Destination ID - Router
        //Next Hop - Router ID of the next hop (In Dijkstra this is also known as Previous Node)
        //Cost - Cost to reach the destination
        Map<String, RouteEntry> distances = new HashMap<>(); //a map of router IDs to RouteEntry objects
        //distances represents the shortest path from the source router to each destination router

        Set<String> visitedNodes = new HashSet<>(); //a set of visited router IDs
        PriorityQueue<RouteEntry> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(entry -> entry.cost));
        // The priority queue is used to select the node with the smallest cost
        // The comparator compares the cost of two RouteEntry objects

        // Initialise the source node
        distances.put(sourceRouterId, new RouteEntry(sourceRouterId, null, 0));
        priorityQueue.add(new RouteEntry(sourceRouterId, null, 0));
        //priority queue is used to select the node with the smallest cost

        while (!priorityQueue.isEmpty()) { // While there are nodes to process
            RouteEntry currentEntry = priorityQueue.poll(); // Get the node with the smallest cost
            //Poll removes the head of the queue and returns it

            String currentNodeId = currentEntry.destinationId; // Get the ID of the current node

            if (visitedNodes.contains(currentNodeId)) {
                continue; // Skip if already visited
            }
            visitedNodes.add(currentNodeId); // Mark the current node as visited

            // Get neighbors of the current router using its LSA
            LSA currentLSA = lsdb.getLSA(currentNodeId);
            if (currentLSA != null) {
                System.out.println("[SPF DEBUG " + java.time.LocalDateTime.now() + " ] Current LSAs links: "
                        + currentLSA.getLinks().toString());

                for (Map.Entry<String, Integer> neighbor : currentLSA.getLinks().entrySet()) // Iterate over neighbors
                {
                    String neighborId = neighbor.getKey();
                    int edgeWeight = neighbor.getValue();

                    if (!visitedNodes.contains(neighborId)) //if the neighbour has not been visited
                    {
                        int newDistance = currentEntry.cost + edgeWeight; // Calculate the new distance
                        if (!distances.containsKey(neighborId) || newDistance < distances.get(neighborId).cost) //if the neighbour is not in the distances map or the new distance is less than the current distance
                        {
                            distances.put(neighborId, new RouteEntry(neighborId, currentNodeId, newDistance)); // Update the distance
                            priorityQueue.add(new RouteEntry(neighborId, currentNodeId, newDistance)); // Add to the priority queue
                        }
                    }
                }
            }
        }

        // Update the routing table
        RoutingTable updatedRoutingTable = buildRoutingTable(distances);
        routingTable.getEntries().clear();
        routingTable.getEntries().putAll(updatedRoutingTable.getEntries());
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
