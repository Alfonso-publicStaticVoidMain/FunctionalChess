package controller.online;

import controller.ChessController;
import functional_chess_model.ChessColor;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;

public class ClientController extends NetworkController {

    public ClientController(ChessController controller) {
        super(controller, ChessColor.BLACK, null);
        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            String host = JOptionPane.showInputDialog(null, "Enter host IP:", "Join Game", JOptionPane.QUESTION_MESSAGE);
            String password = JOptionPane.showInputDialog(null, "Enter password:", "Join Game", JOptionPane.QUESTION_MESSAGE);

            Socket socket = new Socket(host, 5000);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println(password);
            String response = reader.readLine();

            if (!"ACCEPTED".equals(response)) {
                showConnectionLog("Connection rejected by server.");
                socket.close();
                return;
            }

            this.socket = socket;
            this.out = writer;
            this.in = reader;
            listenForMoves();
            showConnectionLog("Connected to server successfully.");

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
            dialog.setTitle("Client Log");
            dialog.add(new JScrollPane(textArea));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            new Timer(4000, e -> dialog.dispose()).start();
        });
    }
}
