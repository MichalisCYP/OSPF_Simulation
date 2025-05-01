package Model;

public class RouteEntry {
    // RouteEntry class represents a single entry in the routing table
    // It contains the destination ID, next hop, and cost to reach the destination
    public String destinationId;
    public String nextHop;
    public int cost;

    public RouteEntry(String destinationId, String nextHop, int cost) {
        this.destinationId = destinationId;
        this.nextHop = nextHop;
        this.cost = cost;
    }
}