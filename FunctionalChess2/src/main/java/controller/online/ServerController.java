package controller.online;

import controller.ChessController;
import functional_chess_model.ChessColor;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.UUID;

public class ServerController extends NetworkController {

    public ServerController(ChessController controller) {
        super(controller, ChessColor.WHITE, null);
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            String password = UUID.randomUUID().toString().substring(0, 6);
            showConnectionLog("Hosting game. Share password: " + password);

            Socket client = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

            String receivedPassword = reader.readLine();
            if (!password.equals(receivedPassword)) {
                writer.println("REJECTED");
                client.close();
                showConnectionLog("Client rejected. Incorrect password.");
                return;
            }

            writer.println("ACCEPTED");

            this.socket = client;
            this.out = writer;
            this.in = reader;
            listenForMoves();
            showConnectionLog("Client connected successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showConnectionLog(String message) {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = new JTextArea(10, 30);
            textArea.setEditable(false);
            textArea.append(LocalTime.now() + " - " + message + "\n");

            JDialog dialog = new JDialog();
            dialog.setTitle("Server Log");
            dialog.add(new JScrollPane(textArea));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            new Timer(4000, e -> dialog.dispose()).start();
        });
    }
}
