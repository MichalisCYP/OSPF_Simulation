package Model;

public class RouteEntry {
    public String destinationId;
    public String nextHop;
    public int cost;

    public RouteEntry(String destinationId, String nextHop, int cost) {
        this.destinationId = destinationId;
        this.nextHop = nextHop;
        this.cost = cost;
    }
}