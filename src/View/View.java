package View;

import Model.RouteEntry;
import Model.RoutingTable;
import java.util.Scanner;

public class View {

    private final Scanner input = new Scanner(System.in);

    public String prompt(String message) {
        System.out.print(message);
        return input.nextLine();
    }

    public void showMessage(String message) {
        System.out.println("[INFO] " + message);
    }

    public void displayRoutingTable(RoutingTable table) {
        System.out.println("=== Routing Table ===");
        for (RouteEntry entry : table.getEntries().values()) {
            System.out.printf("Dest: %s, Next Hop: %s, Cost: %d\n",
                    entry.destinationId, entry.nextHop, entry.cost);
        }
    }

    public void displayStatus(String status) {
        System.out.println("[STATUS] " + status);
    }

    public void showError(String message) {
        System.err.println("[ERROR] " + message);
    }
}
