package Controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Model.Model;
import Model.Neighbour;
import View.View;

public class Controller {
    //Controller handles the interaction between the Model and View
    //starts the server socket in a thread which listens for incoming connections
    //the server socket also spins up a new thread for each incoming connection to
    //handle the neighbour communication

    private final Model model;
    private final View view;
    private ServerSocket serverSocket;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void startRouter() {
        startServerSocket(); // Start the server socket in a separate thread

        while (true) { // Main loop for user commands, using the View to receive input
            String[] cmd = view.prompt("Command (connect/show/status/exit): ").split(" ");
            switch (cmd[0]) {
                case "connect" -> { //connect to a neighbour receiving ip, port and cost
                    if (cmd.length < 4) {
                        view.showMessage("Usage: connect <ip> <port> <cost>");
                        break;
                    }
                    try {
                        //ip, port and cost are parsed from the command line
                        connectToNeighbour(cmd[1], Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                    } catch (NumberFormatException e) {
                        view.showMessage("Invalid cost. Please enter a valid integer for the cost.");
                    }
                }
                case "show" ->
                    //this method is called from the model and returns the routing table
                    view.displayRoutingTable(model.calculateRoutes());
                case "exit" -> {
                    closeServerSocket();
                    return;
                }
                case "status" -> {
                    view.showMessage("Router is running on port " + model.getPort());
                    view.showMessage("Connected neighbours: " + model.getNeighbours());
                }
                case "lsdb" -> {
                    view.showMessage("LSDB: " + model.getLSDB().printLSAs());
                }
                default ->
                    view.showMessage("Unknown command. Use 'connect', 'show', 'status', or 'exit'.");
            }
        }
    }

    //OUTGOING CONNECTION
    private void connectToNeighbour(String ip, int port, int cost) { //outgoing connection to a neighbour 
        //similarly to the server socket, this method creates a new neighbour object and spins up a thread
        //for the neighbour to handle communication
        //only difference is that this method is used for outgoing connections
        try {
            if (model.hasNeighbour("R" + port)) {
                view.showMessage("Already connected to neighbour at " + ip + ":" + port);
                return;
            }

            Socket socket = new Socket(ip, port);
            Neighbour neighbour = new Neighbour(socket, model, view, cost);
            model.addNeighbour(neighbour);
            new Thread(neighbour).start();
            neighbour.sendHello();
        } catch (IOException e) {
            view.showMessage("Failed to connect to neighbour at " + ip + ":" + port);
        }
    }

    //INCOMING CONNECTION
    private void startServerSocket() { // Start the server socket in a new thread
        //this thread accepts incoming connections/neighbours and creates a new thread for each
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(model.getPort());
                view.showMessage("Router listening on port " + model.getPort());
                while (true) {
                    Socket client = serverSocket.accept();
                    handleClientConnection(client);
                }
            } catch (IOException e) {
                view.showMessage("An error occurred while starting the server socket: " + e.getMessage());
            }
        }).start();
    }

    //INCOMING CONNECTION HANDLER
    private void handleClientConnection(Socket client) { //used in the server socket above
        //creates a new neighbour object, adds it to the model and spins up a new thread
        //for the neighbour to handle communication
        String clientAddress = client.getInetAddress().getHostAddress() + ":" + client.getLocalPort();
        System.out.println("Incoming connection from: " + clientAddress);
        if (model.hasNeighbour("R" + client.getLocalPort())) {
            view.showMessage("Duplicate neighbour connection attempt from: " + clientAddress);
            try {
                client.close();
            } catch (IOException e) {
                view.showMessage("Error closing duplicate connection: " + e.getMessage());
            }
            return;
        }

        Neighbour neighbour = new Neighbour(client, model, view, 0);
        model.addNeighbour(neighbour);
        view.showMessage("New neighbour connected: " + clientAddress + " " + client);
        new Thread(neighbour).start();
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                view.showMessage("Server socket closed.");
            }
        } catch (IOException e) {
            view.showMessage("Error closing server socket: " + e.getMessage());
        }
    }
}
