package controller.online;

import configparams.ConfigParameters;
import controller.ChessController;
import functional_chess_model.ChessColor;
import view.online.ConnectionLogger;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ServerController extends NetworkController {

    public ServerController(ChessController controller, Socket socket, BufferedReader in, PrintWriter out) {
        super(controller, socket, in, out);
    }

    public static void startServer(ChessController controller) {
        new Thread(() -> {
            ConnectionLogger logger = new ConnectionLogger();
            try (ServerSocket serverSocket = new ServerSocket(ConfigParameters.SERVER_PORT)) {
                String password = UUID.randomUUID().toString().substring(0, 6);
                String hostAddress = InetAddress.getLocalHost().getHostAddress();

                logger.log("Hosting game. Share password: " + password);
                logger.log("IP address: " + hostAddress);
                logger.log("Waiting for client to connect...");

                Socket clientSocket = serverSocket.accept();
                logger.log("Client attempting to connect from " + clientSocket.getInetAddress());

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String clientPassword = in.readLine();

                if (password.equals(clientPassword)) {
                    logger.log("Password accepted. Starting game.");
                    out.println("ACCEPTED");
                    SwingUtilities.invokeLater(() -> new ServerController(controller, clientSocket, in, out));
                } else {
                    logger.log("Incorrect password. Connection rejected.");
                    out.println("REJECTED");
                    clientSocket.close();
                }

            } catch (IOException e) {
                logger.log("Error starting server: " + e.getMessage());
            }
            logger.waitAndClose();
        }).start();
    }

}
