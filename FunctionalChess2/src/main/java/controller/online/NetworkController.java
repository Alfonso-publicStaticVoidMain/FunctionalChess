package controller.online;

import controller.ChessController;
import functional_chess_model.ChessColor;
import functional_chess_model.GameVariant;
import functional_chess_model.Position;
import view.online.ConnectionLogger;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;

public class NetworkController implements MoveListener {
    protected ChessController chessController;
    protected ChessColor localPlayerColor;
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;

    protected NetworkController(ChessController controller, ChessColor playerColor, Socket socket, BufferedReader in, PrintWriter out) {
        this.chessController = controller;
        this.localPlayerColor = playerColor;
        this.socket = socket;
        this.in = in;
        this.out = out;
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
                    if (chessController.getGame().isValidMove(Position.of(x1, y1), Position.of(x2, y2))) {
                        System.out.println("[DEBUG] Move received: ("+x1+", "+y1+") to ("+x2+", "+y2+")");
                        SwingUtilities.invokeLater(() -> {
                            chessController.handleClick(x1, y1, false);
                            chessController.handleClick(x2, y2, false);
                        });
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void sendMove(int x1, int y1, int x2, int y2) {
        System.out.println("[DEBUG] Move sent: ("+x1+", "+y1+") to ("+x2+", "+y2+")");
        String message = x1 + ":" + y1 + ":" + x2 + ":" + y2;
        out.println(message);
    }

    public boolean isLocalPlayerTurn(ChessColor currentTurn) {
        return currentTurn == localPlayerColor;
    }

    @Override
    public void onMovePerformed(Position initPos, Position finPos) {
        sendMove(initPos.x(), initPos.y(), finPos.x(), finPos.y());
    }
}
