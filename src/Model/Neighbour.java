package Model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import View.View;

public class Neighbour implements Runnable {

    @SuppressWarnings("unused") //read from constructor
    private Socket socket;
    private Model model;
    private DataInputStream in;
    private DataOutputStream out;
    private OSPFState state = OSPFState.DOWN;
    private String id = "UNKNOWN";
    private View view;

    public Neighbour(Socket socket, Model router) {
        this.socket = socket;
        this.model = router;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            view.showError("[Neighbour] Error initializing streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
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

    private void handleMessage(String msg) {
        if (msg.startsWith("HELLO")) {
            String[] parts = msg.split(" ");
            if (parts.length > 1) {
                this.id = parts[1];
            }
            model.receiveHello(this);
        } else if (msg.startsWith("LSA")) {
            // Handle LSA messages
            model.getLSDB().addLSA(LSA.deserialize(msg));
        }
    }

    public void sendHello() {
        try {
            out.writeUTF("HELLO " + model.getRouterId());
            out.flush();
        } catch (IOException e) {
            view.showError("Failed to send HELLO to " + id + ": " + e.getMessage());
        }
    }

    public void sendLSA(LSA lsa) {
        try {
            out.writeUTF("LSA " + lsa.serialize());
            out.flush();
        } catch (IOException e) {
            view.showError("Failed to send LSA to " + id + ": " + e.getMessage());
        }
    }

    public String getId() {
        return id;
    }

    public OSPFState getState() {
        return state;
    }

    public void setState(OSPFState newState) {
        this.state = newState;
    }
}
