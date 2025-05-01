
import Model.LSA;
import Model.LSDB;
import Model.RoutingTable;
import Model.SPFCalculator;
import java.util.Map;

// filepath: /Users/michaelkaramichalis/Developer/OSPF_Simulation/src/CalculatorMain.java
public class CalculatorMain {

    public static void main(String[] args) {
        // Create an instance of LSDB (Link State Database)
        LSDB lsdb = new LSDB();

        // Populate the LSDB with some example data
        lsdb.addLSA(new LSA("5002", Map.of("5003", 3, "5004", 10), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("5003", Map.of("5002", 3, "5004", 2, "5005", 5), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("5004", Map.of("5002", 10, "5003", 2, "5005", 10), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("5005", Map.of("5003", 5, "5004", 10, "5006", 10), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("5006", Map.of("5005", 10), System.currentTimeMillis()));

        // Create an empty RoutingTable
        RoutingTable routingTable = new RoutingTable();

        // Create an instance of SPFCalculator
        SPFCalculator spfCalculator = new SPFCalculator(routingTable, lsdb);

        // Calculate shortest paths starting from "Router1"
        spfCalculator.calculateShortestPaths("5002");

        // Print the resulting routing table
        System.out.println("Routing Table:");
        routingTable.getEntries().forEach((destination, routeEntry) -> {
            System.out.println("Destination: " + destination + ", Cost: " + routeEntry.cost + ", Next Hop: " + routeEntry.nextHop);
        });
    }
}
