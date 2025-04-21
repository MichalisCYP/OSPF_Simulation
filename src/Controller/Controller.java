package Controller;

import Model.Model;
import Model.Neighbour;
import View.View;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {

    //Controller handles the communication between the model and the view
    // It is responsible for starting the server socket, accepting incoming connections,
    private final Model model;
    private final View view;
    private ServerSocket serverSocket; // Server socket to accept incoming connections

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void startRouter() { //this accepts the command line arguments and starts the router
        startServerSocket();

        //this is the command line interface handler
        while (true) {
            String[] cmd = view.prompt("Command (connect/show/status): ").split(" ");
            switch (cmd[0]) {
                case "connect" ->
                    connectToNeighbour(cmd[1], Integer.parseInt(cmd[2]));
                //connect includes: connect localhost 5003 (ip and port)
                case "show" ->
                    view.displayRoutingTable(model.getRoutingTable()); // Show the routing table
                case "exit" -> {
                    try {
                        serverSocket.close(); // Close the server socket
                    } catch (IOException e) {
                        view.showMessage("Error closing server socket: " + e.getMessage());
                    }
                    return; // Exit the loop
                }
                case "status" -> {
                    view.showMessage("Router is running on port " + model.getPort());
                    view.showMessage("Connected neighbours: " + model.getNeighbours());
                }
                default ->
                    view.showMessage("Unknown command. Use 'connect', 'show', or 'exit'.");
            }
        }
    }

    private void startServerSocket() {
        new Thread(() -> { // Start a new thread to handle incoming connections
            try {
                serverSocket = new ServerSocket(model.getPort()); // Create a server socket on the specified port
                view.showMessage("Router listening on port " + model.getPort());
                while (true) {
                    Socket client = serverSocket.accept(); // Accept incoming connection
                    String clientAddress = client.getInetAddress().getHostAddress() + ":" + client.getPort();
                    //get client details

                    //we now have a client connection
                    // Check if the neighbour already exists
                    if (model.hasNeighbour("R" + client.getPort())) {
                        view.showMessage("Duplicate neighbour connection attempt from: " + clientAddress);
                        client.close(); // Close the duplicate connection
                        continue;
                    }

                    // Add the neighbour to the model
                    Neighbour neighbour = new Neighbour(client, model);
                    model.addNeighbour(neighbour);
                    view.showMessage("New neighbour connected: " + clientAddress);

                    // Start a new thread for the neighbour to handle communication 
                    new Thread(neighbour).start();
                }
            } catch (IOException e) {
                view.showMessage("An error occurred while starting the server socket: " + e.getMessage());
            }
        }).start();
    }

    private void connectToNeighbour(String ip, int port) { // Outgoing connection to a neighbour
        try {
            String neighbourAddress = ip + ":" + port;

            // Check if the neighbour already exists
            if (model.hasNeighbour("R" + port)) {
                view.showMessage("Already connected to neighbour at " + neighbourAddress);
                return;
            }

            Socket socket = new Socket(ip, port);
            Neighbour neighbour = new Neighbour(socket, model);
            model.addNeighbour(neighbour);
            new Thread(neighbour).start(); // Start a new thread for the neighbour
            neighbour.sendHello(); // Send HELLO message to the neighbour
        } catch (IOException e) {
            view.showMessage("Failed to connect to neighbour at " + ip + ":" + port);
        }
    }
}
