package Model;

import java.util.*;

public class Model {

    private final String routerId;
    private final int port;
    private final Map<String, Neighbour> neighbours = new HashMap<>();
    private final RoutingTable routingTable = new RoutingTable();

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
        neighbour.setState(OSPFState.TWOWAY);
        addNeighbour(neighbour);
    }
}
