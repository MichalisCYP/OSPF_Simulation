package Model;

import View.View;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Neighbour implements Runnable {

    private Socket socket; //used to connect to the neighbour
    private Model router;
    private DataInputStream in;
    private DataOutputStream out;
    private OSPFState state = OSPFState.DOWN;
    private String id = "UNKNOWN";
    private View view;

    public Neighbour(Socket socket, Model router) {
        this.socket = socket;
        this.router = router;
        try {
            this.in = new DataInputStream(socket.getInputStream()); //used to read data from the socket
            this.out = new DataOutputStream(socket.getOutputStream()); //used to write data to the socket
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
            view.showError("[Neighhour]" + id + " disconnected: " + e.getMessage()
            );
        }
    }

    private void handleMessage(String msg) {
        if (msg.startsWith("HELLO")) {
            String[] parts = msg.split(" ");
            if (parts.length > 1) {
                this.id = parts[1];
            }
            router.receiveHello(this);
        }
    }

    public void sendHello() {
        try {
            out.writeUTF("HELLO " + router.getRouterId());
            out.flush();
        } catch (IOException e) {
            view.showError("Failed to send HELLO to " + id + ": " + e.getMessage());
        }
    }

    public String getId() {
        return id;
    }

    public void setState(OSPFState newState) {
        this.state = newState;
    }
}
