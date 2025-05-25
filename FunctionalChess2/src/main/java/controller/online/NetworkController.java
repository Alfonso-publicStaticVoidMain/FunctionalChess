package controller.online;

import controller.ChessController;
import functional_chess_model.ChessColor;
import functional_chess_model.GameVariant;
import functional_chess_model.Position;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class NetworkController {
    protected ChessController chessController;
    protected ChessColor localPlayerColor;
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;

    public NetworkController(ChessController controller, ChessColor playerColor, Socket socket) {
        this.chessController = controller;
        this.localPlayerColor = playerColor;
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        listenForMoves();
    }

    protected void listenForMoves() {
        Thread listener = new Thread(() -> {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split(":");
                    int x1 = Integer.parseInt(parts[0].trim());
                    int y1 = Integer.parseInt(parts[1].trim());
                    int x2 = Integer.parseInt(parts[2].trim());
                    int y2 = Integer.parseInt(parts[3].trim());
                    Position initPos = Position.of(x1, y1);
                    Position finPos = Position.of(x2, y2);
                    if (chessController.getGame().isValidMove(initPos, finPos)) {
                        SwingUtilities.invokeLater(() -> {
                            chessController.handleClick(x1, y1);
                            chessController.handleClick(x2, y2);
                            //chessController.performNetworkMove(x1, y1, x2, y2);
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void sendMove(int x1, int y1, int x2, int y2) {
        String message = x1 + ":" + y1 + ":" + x2 + ":" + y2;
        out.println(message);
    }

    public boolean isLocalPlayerTurn(ChessColor currentTurn) {
        return currentTurn == localPlayerColor;
    }
}
