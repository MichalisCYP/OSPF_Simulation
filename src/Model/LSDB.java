package Model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LSDB {
    // Link State Database (LSDB) class
    // This class stores all the LSAs received from neighbours
    //LSAs include the router ID, sequence number, and a map of links with their costs
    //The LSDB is used to build the routing table and to perform Dijkstra's algorithm
    //to find the shortest path to each destination

    private final Map<String, LSA> lsaMap;

    public LSDB() {
        this.lsaMap = new HashMap<>();
    }

    public void addLSA(LSA lsa) {
        System.out.println("[DEBUG] [" + java.time.LocalDateTime.now() + "] Adding LSA for Router ID: " + lsa.getAdvertisingRouterId());
        lsaMap.put(lsa.getAdvertisingRouterId(), lsa);
    }

    public LSA getLSA(String routerId) {
        System.out.println("[DEBUG] [" + java.time.LocalDateTime.now() + "] Looking for LSA with Router ID: " + routerId);
        System.out.println("Current LSAs in LSDB: " + lsaMap.keySet());
        if (!lsaMap.containsKey(routerId)) {
            System.out.println("[DEBUG] LSA not found for Router ID: " + routerId);
        }
        return lsaMap.get(routerId);
    }

    public String printLSAs() {
        StringBuilder sb = new StringBuilder();
        sb.append("[LSDB] Current LSAs:\n");
        for (Map.Entry<String, LSA> entry : lsaMap.entrySet()) {
            sb.append("Router ID: ").append(entry.getKey())
                    .append(", LSA: ").append(entry.getValue().serialise()).append("\n");
        }
        return sb.toString();
    }

    public boolean hasLSA(String routerId) {
        return lsaMap.containsKey(routerId);
    }

    public Collection<LSA> getAllLSAs() {
        return lsaMap.values();
    }
}
