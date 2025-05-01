package Controller;

import Model.Model;
import Model.Neighbour;
import View.View;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {
    //Controller handles the interaction between the Model and View
    //starts the server socket in a thread which listens for incoming connections
    //the server socket also spins up a new thread for each incoming connection to
    //handle the neighbour communication seperately

    private final Model model;
    private final View view;
    private Neighbour latestNeighbour; //used to store the neighbour object
    private ServerSocket serverSocket; //for incoming connections

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void startRouter() {
        startServerSocket(); // function starts the server socket in a separate thread

        while (true) { // Main loop for user commands, using the View to receive input
            String[] cmd = view.prompt("Command (connect/show/status/exit): ").split(" "); //split input by spaces

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
                case "port" -> {
                    String port = view.prompt("Enter the new neighbour's port: ");
                    if (port.isEmpty()) {
                        view.showMessage("Port cannot be empty.");
                    } else {
                        addLatestNeighbour(port); //add the latest neighbour to the model
                    }

                }
                case "status" -> {
                    view.showMessage("Router is running on port " + model.getPort());
                    view.showMessage("Connected neighbours: " + model.getNeighbours());
                }
                case "lsdb" -> {
                    view.showMessage("LSDB: " + model.getLSDB().printLSAs());
                }
                case "lsa" -> {
                    //flooding LSA to all neighbours
                    model.floodLSA();
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

            Neighbour neighbour = new Neighbour(socket, String.valueOf(port), model, view, cost); //neighbour object
            model.addNeighbour(neighbour); //add to model map
            new Thread(neighbour).start(); //start the neighbour thread (run function)
            neighbour.sendHello(); //initial hello message
        } catch (IOException e) {
            view.showMessage("Failed to connect to neighbour at " + ip + ":" + port);
        }
    }

    //INCOMING CONNECTION
    private void startServerSocket() { // Start the server socket in a new thread
        //this thread accepts incoming connections/neighbours and creates a new thread for each
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(model.getPort()); //port is set in the model
                view.showMessage("Router listening on port " + model.getPort());
                while (true) {
                    Socket client = serverSocket.accept(); //in case of an incoming connection, accept it
                    handleClientConnection(client); //pass the socket to the handler function
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
        String clientAddress = client.getInetAddress().getHostAddress() + ":" + client.getPort();
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

        //there is a major problem: client.port is manually set by the OS and does not correspond to the port set by the user
        //so we edit it manually
        latestNeighbour = new Neighbour(client, String.valueOf(client.getPort()), model, view, 0);
        //we add the neighbour as the latest neighbour for the user to edit their port
    }

    private void addLatestNeighbour(String port) {
        if (latestNeighbour == null) {
            view.showMessage("No latest neighbour to add.");
            return;
        }
        latestNeighbour.setPort(port); // Set the port of the latest neighbour
        latestNeighbour.setId("R" + port); // Set the ID of the latest neighbour
        model.addNeighbour(latestNeighbour);
        view.showMessage("New neighbour connected: " + port);
        new Thread(latestNeighbour).start();
        latestNeighbour = null;// reset the latest neighbour
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
