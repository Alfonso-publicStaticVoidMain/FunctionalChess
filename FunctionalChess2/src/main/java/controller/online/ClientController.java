package controller.online;

import configparams.ConfigParameters;
import controller.ChessController;
import functional_chess_model.ChessColor;
import graphic_resources.EmergentPanels;
import view.online.ConnectionLogger;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientController extends NetworkController {

    public ClientController(ChessController controller, Socket socket, BufferedReader in, PrintWriter out) {
        super(controller, socket, in, out);
    }

    public static void startClient(ChessController controller) {
        new Thread(() -> {
            ConnectionLogger logger = new ConnectionLogger();
            try {
                String hostAddress = EmergentPanels.userTextInputMessage(null, "Introduce the IP of the host");
                logger.log("Connecting to server at " + hostAddress + ":" + ConfigParameters.SERVER_PORT + "...");
                Socket socket = new Socket(hostAddress, ConfigParameters.SERVER_PORT);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String userPassword = EmergentPanels.userTextInputMessage(null, "Introduce the password");

                out.println(userPassword);

                String response = in.readLine();
                if (response.equals(ConfigParameters.NETWORK_REJECTED)) {
                    logger.log("Wrong password. Server rejected connection.");
                    socket.close();
                } else {
                    logger.log("Connection accepted. Game is starting...");

                    SwingUtilities.invokeLater(() -> {
                        new ClientController(controller, socket, in, out);
                    });
                }

            } catch (IOException e) {
                logger.log("Failed to connect: " + e.getMessage());
            }
            logger.waitAndClose();
        }).start();
    }

}
