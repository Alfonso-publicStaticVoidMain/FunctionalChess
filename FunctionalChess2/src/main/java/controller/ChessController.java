package controller;

import configparams.ConfigParameters;
import controller.online.MoveListener;
import functional_chess_model.*;
import functional_chess_model.Pieces.King;
import functional_chess_model.Pieces.Pawn;
import graphic_resources.EmergentPanels;
import view.ChessGUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.*;

/**
 * Class that controls the {@link ChessGUI} view of a given chess game
 * according to its {@link Chess} model game.
 * @author Alfonso Gallego Fernández
 */
public class ChessController implements ActionListener {
    
    /**
     * {@link Chess} game the controller is controlling.
     */
    private Chess game;
    /**
     * {@link ChessGUI} view the controller is controlling.
     */
    private final ChessGUI view;
    /**
     * {@link Position} stored to fetch a piece to move from it.
     */
    private Position selectedPosition;

    private int whiteSecondsLeft;
    private int blackSecondsLeft;

    private final boolean isOnlineGame;
    private final ChessColor localPlayer;

    private final List<MoveListener> moveListeners = new ArrayList<>();

    /**
     * General constructor permitting the creation of online games.
     * @param game {@link Chess} game this controller is controlling.
     * @param view {@link ChessGUI} view this controller is controlling.
     * @param isOnlineGame State parameter to track if this game is played online or not.
     * @param localPlayer Only makes sense to give this a value if isOnlineGame is set
     * to true, tracking what color the local player is playing.
     */
    public ChessController(Chess game, ChessGUI view, boolean isOnlineGame, ChessColor localPlayer) {
        this.game = game;
        this.view = view;
        this.isOnlineGame = isOnlineGame;
        this.localPlayer = localPlayer;
        this.whiteSecondsLeft = game.whiteSeconds();
        this.blackSecondsLeft = game.blackSeconds();
        this.view.setController(this);
        this.view.addActionListeners();
        this.view.updateBoard();
        this.selectedPosition = null;
    }

    public ChessController(Chess game, ChessGUI view) {
        this(game, view, false, null);
    }

    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    /**
     * Getter for the game attribute of the controller.
     * @return The {@link Chess} game the controller is controlling.
     */
    public Chess getGame() {return game;}

    public void setGame(Chess game) {this.game = game;}

    /**
     * Consumes one second from the white player's seconds left.
     */
    public void consumeWhiteSecond() {whiteSecondsLeft--;}

    /**
     * Consumes one second from the black player's seconds left.
     */
    public void consumeBlackSecond() {blackSecondsLeft--;}

    /**
     * Creates a Timer to track and update the time left for each player.
     * @param whiteTimer JLabel containing the seconds left for the white player.
     * @param blackTimer JLabel containing the seconds left for the black player.
     * @return A Timer that each second it paints the active player's timer RED,
     * the inactive player's BLACK, and consumes one second from the
     * {@link ChessController}'s respective {@code whiteSeconds} or
     * {@code blackSeconds} attribute, then checks if that player's seconds are
     * zero, and in that case, it finishes the game.
     */
    public Timer viewTimer(JLabel whiteTimer, JLabel blackTimer) {
        return new Timer(1000, e -> {
            if (game.state() == GameState.IN_PROGRESS) {

                if (game.activePlayer() == ChessColor.WHITE) {
                    consumeWhiteSecond();
                    blackTimer.setForeground(Color.BLACK);
                    whiteTimer.setForeground(Color.RED);
                    whiteTimer.setText(formatTime(whiteSecondsLeft));
                    if (whiteSecondsLeft == 0) {
                        ((Timer) e.getSource()).stop();
                        JOptionPane.showMessageDialog(view, "White ran out of time!");
                        game = new Chess(game.pieces(), game.castling(), game.playHistory(), game.activePlayer(), game.variant(), GameState.BLACK_WINS, game.isTimed(), game.whiteSeconds(), game.blackSeconds());
                    }
                } else {
                    consumeBlackSecond();
                    blackTimer.setForeground(Color.RED);
                    whiteTimer.setForeground(Color.BLACK);
                    blackTimer.setText(formatTime(blackSecondsLeft));
                    if (blackSecondsLeft <= 0) {
                        ((Timer) e.getSource()).stop();
                        JOptionPane.showMessageDialog(view, "Black ran out of time!");
                        game = new Chess(game.pieces(), game.castling(), game.playHistory(), game.activePlayer(), game.variant(), GameState.WHITE_WINS, game.isTimed(), game.whiteSeconds(), game.blackSeconds());
                    }
                }
            }
        });
    }

    /**
     * Part of the action listener for the view's buttons on the chess board.
     * @param x X coordinate of the button clicked.
     * @param y Y coordinate of the button clicked.
     */
    public void handleClick(int x, int y, boolean sendMove, String crowningType) {
        view.clearHighlights();
        if (localPlayer != null && sendMove && localPlayer != game.activePlayer()) return; // For online games, do not permit the nonactive player to move
        if (x == 0 || y == 0) return; // Ignore label clicks
        if (game.state().hasEnded()) return; // Don't do anything if the game has ended.
        
        Position clickedPos = Position.of(x, y);
        
        if (selectedPosition == null) { // First click stores the selected piece.
            if (game.checkPieceAt(clickedPos)) {
                Piece piece = game.findPieceAt(clickedPos).get();
                if (piece.getColor() == game.activePlayer()) {
                    selectedPosition = clickedPos;
                    view.highlightValidMoves(piece);
                } else {
                    view.highlightMovesOfEnemyPiece(piece);
                }
            }
        } else { // Second click attempts to do the movement.
            Piece piece = game.findPieceAt(selectedPosition).get();
            boolean playDone = false;
            
            if (!piece.isLegalMovement(game, clickedPos)) {
                view.highlightPiecesThatCanCaptureKing(piece, clickedPos);
            }
                
            if (piece instanceof King) {
                for (CastlingType type : CastlingType.values()) {
                    if (!playDone) {
                        Optional<CastlingType> castlingTypeOfPlay = game.castlingTypeOfPlay(piece, clickedPos);
                        if (castlingTypeOfPlay.isPresent() && type == castlingTypeOfPlay.get()) {
                            Optional<Chess> gameAfterCastling = game.tryToCastle(game.activePlayer(), type);
                            if (gameAfterCastling.isPresent()) {
                                game = gameAfterCastling.get();
                                playDone = true;
                            }
                        }
                    }
                }                
            }
                
            if (!playDone) {
                Optional<Chess> gameAfterMoveOrNot = game.tryToMove(piece, clickedPos);
                if (gameAfterMoveOrNot.isPresent()) {
                    game = gameAfterMoveOrNot.get();
                    playDone = true;
                }
            }
            
            if (playDone) {

                String crownedType = null;
                piece = game.findPieceAt(clickedPos).orElse(piece);

                if (piece instanceof Pawn && piece.getPosition().y() == game.variant().crowningRow(game.activePlayer().opposite())) {
                    view.updateBoard();
                    game = game.crownPawnChain(piece, crowningType != null ? crowningType : EmergentPanels.pawnCrowningMenu(view, game.variant().crownablePieces()));
                    piece = game.findPieceAt(clickedPos).orElse(piece);
                    crownedType = piece.getClass().getSimpleName();
                }
                if (isOnlineGame && sendMove) notifyMovePerformed(selectedPosition, clickedPos, crowningType);
                Optional<Play> lastPlay = game.getLastPlay();
                lastPlay.ifPresent(view::updatePlayHistory);
                view.updateBoard();

                view.updateActivePlayer();

                game = game.checkMateChain(game.activePlayer());
                if (game.state() == GameState.WHITE_WINS || game.state() == GameState.BLACK_WINS) {
                    view.checkMessage(game.activePlayer());
                } else if (game.state() == GameState.DRAW) {
                    view.drawMessage(game.activePlayer());
                }
            }

            selectedPosition = null;
            game = game.withSeconds(whiteSecondsLeft, blackSecondsLeft);
        }
    }

    public void handleClick(int x, int y, boolean sendMove) {
        handleClick(x, y, sendMove, null);
    }

    public void handleClick(int x, int y) {
        handleClick(x, y, true);
    }

    public void setGameState(Chess game) {
        this.game = game;
        if (game.isTimed()) {
            whiteSecondsLeft = game.whiteSeconds();
            blackSecondsLeft = game.blackSeconds();
        }
        view.updateBoard();
        view.updateActivePlayer();
        view.reloadPlayHistory();
    }

    public void setDefaultGameState() {
        setGameState(game.variant().initGame(game.isTimed()));
    }
    
    /**
     * Part of the action listener for the reset button on the view. It changes
     * the game attribute of the controller to the initial state of the game
     * with the configuration currently being used.
     */
    public void resetClick() {
        if (!EmergentPanels.askConfirmation(view, "Do you want to reset the game?")) return;
        setDefaultGameState();
    }
    
    /**
     * Shows a menu to ask confirmation from the user, then if they confirm,
     * shows a menu to let them write some text that will be the file name
     * that will be saved in savedgames/[name].dat, containing the information
     * about the current state of the game.
     */
    public void saveClick() {
        if (!EmergentPanels.askConfirmation(view, "Do you want to save the state of the game?")) return;
        String filePath = null;
        try {
            filePath = EmergentPanels.userTextInputMessage(view,"Enter the name of your game");
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
            return;
        }
        try (
            FileOutputStream fos = new FileOutputStream("savedgames"+File.separator+filePath+".dat", false);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos))
        {
            oos.writeObject(game);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
    }
    
    /**
     * Shows a menu to ask confirmation from the user, then if they confirm,
     * loads a fileChooser to allow them to select a file stored in savedgames
     * to set the game to the game stored in the file, then updating the board,
     * active player and play history.
     * <br><br>
     * If the stored game isn't of the same dimensions as the current game,
     * shows an error message and cancels the load. If it's of the same
     * dimensions but of a different variant, lets the load happen but still
     * shows a warning message.
     */
    public void loadClick() {
        boolean userVerification = EmergentPanels.askConfirmation(view, "Do you want to load a saved game?");
        if (!userVerification) return;
        try (
            FileInputStream fis = new FileInputStream(EmergentPanels.fileChooser("." + File.separator + "savedgames"));
            BufferedInputStream bufis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bufis)
        ) {
            Chess chessGame = (Chess) ois.readObject();
            if (chessGame.variant().rows() == game.variant().rows() && chessGame.variant().cols() == game.variant().cols()) {
                boolean playerChoice = true;
                if (chessGame.variant() != game.variant()) {
                    playerChoice = EmergentPanels.askConfirmation(view, "The game you wanted to load is of variant: " + chessGame.variant()
                        + ", while you're playing " + game.variant() +
                        "\nBut thankfully they are compatible in size. Do you still want to load that game?");
                }
                if (playerChoice) setGameState(chessGame);
            } else {
                EmergentPanels.informPlayer(view, "Incompatible dimensions", "Your selected game is of variant "
                    + chessGame.variant() + " (" + chessGame.variant().rows() + "x" + chessGame.variant().cols()
                    + "), while your current one is " + game.variant() + " (" + game.variant().rows() + "x" + game.variant().cols() + ")");
            }

        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Class not found: " + ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] ChessController action received: "+command);
        switch (command) {
            case ConfigParameters.BOARD_BUTTON -> {
                JButton clickedButton = (JButton) e.getSource();
                int x = (int) clickedButton.getClientProperty("x");
                int y = (int) clickedButton.getClientProperty("y");
                System.out.println("[DEBUG] Position: "+Position.of(x, y)+" (x="+x+", y="+y+")");
                handleClick(x, y);
            }
            case ConfigParameters.RESET_BUTTON -> resetClick();
            case ConfigParameters.SAVE_BUTTON -> saveClick();
            case ConfigParameters.LOAD_BUTTON -> loadClick();
            case ConfigParameters.BACK_BUTTON -> SwingUtilities.invokeLater(() -> {
                boolean userVerification = game.state() == GameState.NOT_STARTED
                    || EmergentPanels.askConfirmation(view, "Do you want to go back to the index?\nYou'll lose the state of the game unless you saved it.");
                if (userVerification) {
                    view.dispose();
                    new IndexController();
                }
            });
        }
    }

    public void addMoveListener(MoveListener listener) {
        moveListeners.add(listener);
    }

    public void clearMoveListeners() {
        moveListeners.clear();
    }

    public void removeMoveListener(MoveListener listener) {
        moveListeners.remove(listener);
    }

    private void notifyMovePerformed(Position initPos, Position finPos, String crowningType) {
        for (MoveListener listener : moveListeners) {
            listener.onMovePerformed(initPos, finPos, crowningType);
        }
    }

}