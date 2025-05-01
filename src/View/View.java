package View;

import Model.RouteEntry;
import Model.RoutingTable;
import java.util.Scanner;

public class View {

    private final Scanner input = new Scanner(System.in); // Scanner for user input, which is used to read commands from the console

    public String prompt(String message) {
        System.out.print(message);
        return input.nextLine();
    }

    public void showMessage(String message) {
        System.out.println("[INFO " + java.time.LocalDateTime.now() + " ] " + message);
    }

    public void displayRoutingTable(RoutingTable table) {
        System.out.println("=== Routing Table ===");
        for (RouteEntry entry : table.getEntries().values()) {
            System.out.printf("Dest: %s, Next Hop: %s, Cost: %d\n",
                    entry.destinationId, entry.nextHop, entry.cost);
        }
    }

    public void displayStatus(String status) {
        System.out.println("[STATUS " + java.time.LocalDateTime.now() + " ] " + status);
    }

    public void showError(String message) {
        System.err.println("[ERROR " + java.time.LocalDateTime.now() + " ] " + message);
    }
}
