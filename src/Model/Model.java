package Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Model {

    private final String routerId;
    private final int port;
    private final Map<String, Neighbour> neighbours = new HashMap<>();
    private final RoutingTable routingTable = new RoutingTable();
    private final LSDB lsdb = new LSDB();

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

    public void removeNeighbour(String id) {
        neighbours.remove(id);
    }

    public void receiveHello(Neighbour neighbour) {
        System.out.println("[OSPF] Received HELLO from " + neighbour.getId());
        if (neighbour.getState() == OSPFState.DOWN) {
            neighbour.setState(OSPFState.TWOWAY);
            neighbour.sendHello();
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

    public void receiveLSA(LSA lsa) {
        lsdb.addLSA(lsa);
        System.out.println("[OSPF] Received LSA from " + lsa.getAdvertisingRouterId());
        // Further processing can be done here, such as updating the routing table
    }

    public LSDB getLSDB() {
        return lsdb;
    }

    public void calculateRoutes() {
        SPFCalculator spfCalculator = new SPFCalculator(routingTable, lsdb);
        spfCalculator.calculateShortestPaths(routerId);
    }
}
