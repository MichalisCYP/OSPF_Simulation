package Controller;

import Model.Model;
import Model.Neighbour;
import View.View;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
            String[] cmd = view.prompt("Command (connect/show): ").split(" ");
            if (cmd[0].equals("connect")) {
                connectToNeighbour(cmd[1], Integer.parseInt(cmd[2]));
            } else if (cmd[0].equals("show")) {
                view.displayRoutingTable(model.getRoutingTable());
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
                    Neighbour neighbour = new Neighbour(client, model);
                    model.addNeighbour(neighbour);
                    new Thread(neighbour).start();
                }
            } catch (IOException e) {
                view.showMessage("An error occurred while starting the server socket: " + e.getMessage());
            }
        }).start();
    }

    private void connectToNeighbour(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            Neighbour neighbour = new Neighbour(socket, model);
            model.addNeighbour(neighbour);
            new Thread(neighbour).start();
            neighbour.sendHello();
        } catch (IOException e) {
            view.showMessage("Failed to connect to neighbour at " + ip + ":" + port);
        }
    }
}