package Model;

import View.View;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Neighbour implements Runnable {

    // Neighbour class represents a connection to another OSPF router
    // It handles the communication with the neighbour, including sending and receiving messages
    // It also maintains the state of the neighbour and its cost
    // The neighbour can be in one of the following states: DOWN, INIT, TWOWAY, EXSTART, EXCHANGE, LOADING, FULL
    // The cost is the metric used to determine the best path to the neighbour
    //In this simulation the cost is set by the user when connecting to a neighbour
    //In the real world the cost is determined by the Default Bandwidth of the link / Interface Bandwidth
    @SuppressWarnings("unused") //socket read from constructor, used to avoid warning
    private Socket socket;
    @SuppressWarnings("unused") //port read from constructor, used to avoid warning
    private String port; //port of the neighbour (string since its used for IDs and manually set)
    private Model model; //Router
    //Data Streams, used to send and receive messages through the socket
    private DataInputStream in;
    private DataOutputStream out;
    private OSPFState state = OSPFState.DOWN; //initial state
    private String id = "UNKNOWN";
    private View view;
    private int cost = 0;

    public Neighbour(Socket socket, String port, Model router, View view, int cost) {
        this.socket = socket;
        this.model = router;
        this.view = view;
        this.cost = cost;
        this.port = port;

        // Use the remote port as the ID for incoming connections
        this.id = "R" + port;
        System.out.println("Initialised neighbour with " + this.id);

        try {
            // Initialise the input and output streams for the socket
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            view.showError("[Neighbour] Error initializing streams: " + e.getMessage());
        }
    }

    @Override
    public void run() { //main loop for the neighbour thread 
        //handles incoming messages from the neighbour
        //UTF is used to receive messages through the socket
        try {
            while (true) {
                String msg = in.readUTF();
                handleMessage(msg);
            }
        } catch (IOException e) {
            view.showError("[Neighbour] " + id + " disconnected: " + e.getMessage());
            model.removeNeighbour(this.id); // Remove the neighbour from the model
        }
    }

    private void handleMessage(String msg) { // Handle incoming messages from the neighbour
        if (msg.startsWith("HELLO")) {
            String[] parts = msg.split(" ");
            if (parts.length > 2) {
                this.id = parts[1];
                try {
                    this.cost = Integer.parseInt(parts[2]); // Update cost from the message
                } catch (NumberFormatException e) {
                    view.showError("[Neighbour] Invalid cost received in HELLO message: " + parts[2]);
                }
            }
            model.receiveHello(this);
        } else if (msg.startsWith("LSA")) {
            LSA lsa = LSA.deserialise(msg.substring(4));
            model.receiveLSA(lsa);
        }
        //we could also implement OSPF in more detail, including LSDB exchange, LSRs, LSACKs, but for now we will keep it simple
    }

    public void sendHello() { // Send HELLO message to the neighbour
        try {
            out.writeUTF("HELLO " + model.getRouterId() + " " + cost);
            out.flush(); //flush means send the message immediately to ens
        } catch (IOException e) {
            view.showError("Failed to send HELLO to " + id + ": " + e.getMessage());
        }
    }

    public void sendLSA(LSA lsa) { // Send LSA to the neighbour through the socket
        try {
            out.writeUTF("LSA " + lsa.serialise());
            out.flush();
        } catch (IOException e) {
            view.showError("Failed to send LSA to " + id + ": " + e.getMessage());
        }
    }

    //#region Getters and Setters
    public String getId() {
        return id;
    }

    public OSPFState getState() {
        return state;
    }

    public void setState(OSPFState newState) {
        this.state = newState;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setId(String id) {
        this.id = id;
    }
    //#endregion
}
