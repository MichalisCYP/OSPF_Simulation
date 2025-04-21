package Controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Model.Model;
import Model.Neighbour;
import View.View;

public class Controller {

    private final Model model;
    private final View view;
    private ServerSocket serverSocket;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void startRouter() {
        startServerSocket();

        while (true) {
            String[] cmd = view.prompt("Command (connect/show/status/exit): ").split(" ");
            switch (cmd[0]) {
                case "connect" -> connectToNeighbour(cmd[1], Integer.parseInt(cmd[2]));
                case "show" -> view.displayRoutingTable(model.getRoutingTable());
                case "exit" -> {
                    closeServerSocket();
                    return;
                }
                case "status" -> {
                    view.showMessage("Router is running on port " + model.getPort());
                    view.showMessage("Connected neighbours: " + model.getNeighbours());
                }
                default -> view.showMessage("Unknown command. Use 'connect', 'show', 'status', or 'exit'.");
            }
        }
    }

    private void startServerSocket() {
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

    private void handleClientConnection(Socket client) {
        String clientAddress = client.getInetAddress().getHostAddress() + ":" + client.getPort();
        if (model.hasNeighbour("R" + client.getPort())) {
            view.showMessage("Duplicate neighbour connection attempt from: " + clientAddress);
            try {
                client.close();
            } catch (IOException e) {
                view.showMessage("Error closing duplicate connection: " + e.getMessage());
            }
            return;
        }

        Neighbour neighbour = new Neighbour(client, model);
        model.addNeighbour(neighbour);
        view.showMessage("New neighbour connected: " + clientAddress);
        new Thread(neighbour).start();
    }

    private void connectToNeighbour(String ip, int port) {
        try {
            if (model.hasNeighbour("R" + port)) {
                view.showMessage("Already connected to neighbour at " + ip + ":" + port);
                return;
            }

            Socket socket = new Socket(ip, port);
            Neighbour neighbour = new Neighbour(socket, model);
            model.addNeighbour(neighbour);
            new Thread(neighbour).start();
            neighbour.sendHello();
        } catch (IOException e) {
            view.showMessage("Failed to connect to neighbour at " + ip + ":" + port);
        }
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