package Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Model {

    // Model class for the OSPF router
    // This class represents the core logic of the router, 
    //including its ID, port, neighbours, routing table, and LSDB
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

    public void receiveHello(Neighbour neighbour) { //called from the neighbour thread when a hello message is received
        System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Received HELLO from " + neighbour.getId() + " with cost " + neighbour.getCost());
        if (neighbour.getState() == OSPFState.DOWN) {
            neighbour.setState(OSPFState.TWOWAY);
            // neighbour.sendHello();
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Neighbour " + neighbour.getId() + " is now in state " + neighbour.getState());
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Moving to the Exchange state");
            neighbour.setState(OSPFState.EXCHANGE);

            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Sending LSA to " + neighbour.getId());
            sendLSA(neighbour);
        }
        // now we proceed to the next state
    }

    public void sendLSA(Neighbour neighbour) { //send LSA to the neighbour
        // Create a new LSA with the current router's ID, LSDB (its neighbors), and sequence number
        Map<String, Integer> links = new HashMap<>();
        for (Map.Entry<String, Neighbour> entry : neighbours.entrySet()) {
            links.put(entry.getKey(), entry.getValue().getCost());
        }
        LSA lsa = new LSA(routerId, links, 1);

        neighbour.sendLSA(lsa);
        System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Sent LSA to " + neighbour.getId());
    }

    public boolean hasNeighbour(String id) {
        return neighbours.containsKey(id);
    }

    public Set<String> getNeighbours() {
        return neighbours.keySet();
    }

    public void receiveLSA(LSA lsa) { //called from the neighbour thread when an LSA message is received
        // lsdb.addLSA(lsa);
        System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Received LSA from " + lsa.getAdvertisingRouterId());
        //check if the LSA is already in the LSDB
        if (lsdb.hasLSA(lsa.getAdvertisingRouterId())) {
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] LSA already exists in LSDB");
        } else {
            //if it doesn't exist, add it to the LSDB and flood updated LSA to all neighbours
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Adding LSA to LSDB");
            lsdb.addLSA(lsa);
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Flooding LSA to all neighbours");
            floodLSA();
        }
    }

    public void updateNeighbourCost(String neighbourId, int cost) {
        Neighbour neighbour = neighbours.get(neighbourId);
        if (neighbour != null) {
            neighbour.setCost(cost);
            System.out.println("[OSPF] Updated cost for neighbour " + neighbourId + " to " + cost);
        } else {
            System.out.println("[OSPF] Neighbour " + neighbourId + " not found.");
        }
    }

    public LSDB getLSDB() {
        return lsdb;
    }

    public void floodLSA() {
        Map<String, Integer> links = new HashMap<>();
        for (Map.Entry<String, Neighbour> entry : neighbours.entrySet()) {
            links.put(entry.getKey(), entry.getValue().getCost());
        }
        LSA lsa = new LSA(routerId, links, 1); //this router's LSA
        System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Flooding LSA: " + lsa.serialize());
        // Flood the LSA to all neighbours
        for (Neighbour neighbour : neighbours.values()) {
            neighbour.sendLSA(lsa);
            System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Flooded LSA to " + neighbour.getId());
        }
    }

    public RoutingTable calculateRoutes() {
        System.out.println("[OSPF " + java.time.LocalDateTime.now() + " ] Calculating routes using Dijkstra's algorithm");
        System.out.println("LSAs: " + lsdb.printLSAs());
        System.out.println("for router: " + routerId);
        SPFCalculator spfCalculator = new SPFCalculator(routingTable, lsdb);
        spfCalculator.calculateShortestPaths(routerId);
        return spfCalculator.getRoutingTable();
    }
}
