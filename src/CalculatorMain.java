
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
        lsdb.addLSA(new LSA("Router1", Map.of("Router2", 10, "Router3", 15), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("Router2", Map.of("Router1", 10, "Router4", 20), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("Router3", Map.of("Router1", 15, "Router4", 25), System.currentTimeMillis()));
        lsdb.addLSA(new LSA("Router4", Map.of("Router2", 20, "Router3", 25), System.currentTimeMillis()));

        // Create an empty RoutingTable
        RoutingTable routingTable = new RoutingTable();

        // Create an instance of SPFCalculator
        SPFCalculator spfCalculator = new SPFCalculator(routingTable, lsdb);

        // Calculate shortest paths starting from "Router1"
        spfCalculator.calculateShortestPaths("Router1");

        // Print the resulting routing table
        System.out.println("Routing Table:");
        routingTable.getEntries().forEach((destination, routeEntry) -> {
            System.out.println("Destination: " + destination + ", Cost: " + routeEntry.cost + ", Next Hop: " + routeEntry.nextHop);
        });
    }
}
