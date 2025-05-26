package controller.online;

import controller.ChessController;
import functional_chess_model.Chess;
import functional_chess_model.ChessColor;
import functional_chess_model.GameVariant;
import functional_chess_model.Position;
import view.online.ConnectionLogger;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.stream.IntStream;

public class NetworkController implements MoveListener {
    protected ChessController chessController;
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;

    protected NetworkController(ChessController controller, Socket socket, BufferedReader in, PrintWriter out) {
        this.chessController = controller;
        this.chessController.addMoveListener(this);
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
                    int y2;
                    if (x2 == 0) {
                        y2 = Integer.parseInt(parts[3].trim());
                        Position initPos = Position.of(x1, y1);
                        Position finPos = Position.of(x2, y2);
                        if (chessController.getGame().isValidMove(Position.of(x1, y1), Position.of(x2, y2))) {
                            System.out.println("[NETWORK DEBUG] Move received: "+ initPos +" ("+x1+", "+y1+") to "+ finPos +" ("+x2+", "+y2+")");
                            SwingUtilities.invokeLater(() -> {
                                chessController.handleClick(x1, y1, false);
                                chessController.handleClick(x2, y2, false);
                            });
                        }
                    } else {
                        String pieceType = parts[3].trim();
                        Position pos = Position.of(x1, y1);
                        Chess game = chessController.getGame();
                        if (game.checkPieceAt(pos)) {
                            game.crownPawn(game.findPieceAt(pos).get(), pieceType);
                        }
                    }


                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    @Override
    public void onMovePerformed(Position initPos, Position finPos) {
        System.out.println("[NETWORK DEBUG] Move sent: ("+initPos.x()+", "+initPos.y()+") to ("+finPos.x()+", "+finPos.y()+")");
        out.println(initPos.x() + ":" + initPos.y() + ":" + finPos.x() + ":" + finPos.y());
    }

    @Override
    public void onCrowningPerformed(Position pos, String pieceType) {
        out.println(pos.x() + ":" + pos.y() +  ":" + 0 + ":" + pieceType);
    }
}
