package Model;

import java.util.*;

public class Model {

    private final String routerId;
    private final int port; // Port number for the router to listen on for incoming connections
    private final Map<String, Neighbour> neighbours = new HashMap<>(); // Map to store neighbours
    private final RoutingTable routingTable = new RoutingTable(); // Routing table to store routing information

    public Model(String routerId, int port) {
        this.routerId = routerId;
        this.port = port;
    }

    public String getRouterId() {
        return routerId;
    }

    public int getPort() {
        return port;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void addNeighbour(Neighbour neighbour) {
        neighbours.put(neighbour.getId(), neighbour);
    }

    public void receiveHello(Neighbour neighbour) {
        System.out.println("[OSPF] Received HELLO from " + neighbour.getId());
        if (neighbour.getState() == OSPFState.DOWN) { // Only respond if the state is DOWN
            neighbour.setState(OSPFState.TWOWAY); // Set the state to TWOWAY
            neighbour.sendHello(); // Send HELLO back to the neighbour
            addNeighbour(neighbour);
            System.out.println("[OSPF] Neighbour " + neighbour.getId() + " is now in state " + neighbour.getState());
        }
    }

    public boolean hasNeighbour(String id) {
        return neighbours.containsKey(id);
    }

    public Set<String> getNeighbours() {
        return neighbours.keySet();
    }
}
