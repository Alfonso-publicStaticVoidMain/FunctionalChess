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
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.Timer;

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

    public ChessGUI getView() {return view;}

    /**
     * Consumes one second from the white player's seconds left.
     */
    public void consumeWhiteSecond() {whiteSecondsLeft--;}

    /**
     * Consumes one second from the black player's seconds left.
     */
    public void consumeBlackSecond() {blackSecondsLeft--;}

    public List<Position> positionsThatValidate(Predicate<Position> condition) {
        return IntStream.rangeClosed(1, game.variant().rows())
            .boxed()
            .flatMap(row ->
                IntStream.rangeClosed(1, game.variant().cols())
                    .mapToObj(col -> Position.of(col, row))
            )
            .filter(condition)
            .toList();
    }

    public List<Position> validMovesOf(Piece piece) {
        return positionsThatValidate(pos -> (piece.isLegalMovement(game, pos) ||
            (piece instanceof King && game.castlingTypeOfPlay(piece, pos).isPresent())));
    }

    public List<Position> validMovesThatWouldCauseCheckOf(Piece piece) {
        return positionsThatValidate(pos -> piece.isLegalMovement(game, pos, false));
    }

    public List<Position> piecesThatCanCaptureKing(Piece piece, Position finPos) {
        Chess gameAfterMovement = game.tryToMoveChain(piece, finPos, false);
        ChessColor color = piece.getColor();
        Optional<Piece> royalPieceOrNot = gameAfterMovement.findRoyalPiece(color);
        if (royalPieceOrNot.isEmpty()) return List.of();

        return gameAfterMovement.pieces().stream()
            .filter(p -> // Filter for the initPieces of a different color than active player that can move to capture active player's King.
                p.getColor() != color &&
                    p.isLegalMovement(gameAfterMovement, royalPieceOrNot.get().getPosition(), false)
                )
            .map(Piece::getPosition)
            .toList();
    }

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
     * @param sendMove State parameter to track if the move will be sent to the server/client in an online game.
     * @param crowningType Type to crown a {@link Pawn} into, if the parameter is not null, instead of showing
     * the crowning menu.
     */
    public void handleClick(int x, int y, boolean sendMove, String crowningType) {
        view.clearHighlights();

        if (x == 0 || y == 0) return; // Ignore label clicks
        if (game.state().hasEnded()) return; // Don't do anything if the game has ended.
        
        Position clickedPos = Position.of(x, y);

        if (localPlayer != null && sendMove && localPlayer != game.activePlayer()) {
            /*
            For online games, do not permit the nonactive player to move and only show the possible moves of
            the piece in the position clicked, if present.
             */
            game.findPieceAt(clickedPos).ifPresent(piece -> view.highlightValidMovesOf(piece, Color.YELLOW, 1000));
            return;
        }

        if (selectedPosition == null) { // First click stores the selected piece and shows possible moves.
            Optional<Piece> pieceOrNot = game.findPieceAt(clickedPos);
            if (pieceOrNot.isPresent()) {
                Piece piece = pieceOrNot.get();
                if (piece.getColor() == game.activePlayer()) {
                    selectedPosition = clickedPos;
                    view.highlightValidMovesOf(piece, Color.GREEN);
                    view.highlightValidMovesThatWouldCauseCheckOf(piece, Color.ORANGE);
                } else {
                    view.highlightValidMovesOf(piece, Color.YELLOW, 1000);
                }
            }
        } else { // Second click attempts to do the movement.
            Piece piece = game.findPieceAt(selectedPosition).get();
            boolean playDone = false;
            
            if (!piece.isLegalMovement(game, clickedPos)) {
                view.highlightPiecesThatCanCaptureKing(piece, clickedPos, Color.RED, 1000);
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
                    if (crowningType == null) crownedType = EmergentPanels.pawnCrowningMenu(view, game.variant().crownablePieces());
                    game = game.crownPawnChain(piece, crowningType != null ? crowningType : crownedType);
                }

                if (isOnlineGame && sendMove) notifyMovePerformed(selectedPosition, clickedPos, crowningType != null ? crowningType : crownedType);
                Optional<Play> lastPlay = game.getLastPlay();
                lastPlay.ifPresent(view::updatePlayHistory);
                view.updateBoard();
                view.updateActivePlayer(game.activePlayer().toString());

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
        handleClick(x, y, true, null);
    }

    public void setGame(Chess game) {
        this.game = game;
        if (game.isTimed()) {
            whiteSecondsLeft = game.whiteSeconds();
            blackSecondsLeft = game.blackSeconds();
        }
        view.updateBoard();
        view.updateActivePlayer(this.game.activePlayer().toString());
        view.reloadPlayHistory();
    }

    public void setDefaultGame() {
        setGame(game.variant().initGame(game.isTimed()));
    }
    
    /**
     * Part of the action listener for the reset button on the view. It changes
     * the game attribute of the controller to the initial state of the game
     * with the configuration currently being used.
     */
    public void resetClick() {
        if (!EmergentPanels.askConfirmation(view, "Do you want to reset the game?")) return;
        setDefaultGame();
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
                if (playerChoice) setGame(chessGame);
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

    private void backClick() {
        SwingUtilities.invokeLater(() -> {
            boolean userVerification = game.state() == GameState.NOT_STARTED
                    || EmergentPanels.askConfirmation(view, "Do you want to go back to the index?\nYou'll lose the state of the game unless you saved it.");
            if (userVerification) {
                view.dispose();
                new IndexController();
            }
        });
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
            case ConfigParameters.RESET_BUTTON -> {
                if (isOnlineGame) {
                    EmergentPanels.informPlayer(view, "You can't do this on an online game!", "You can't reset the game during an online game.");
                    return;
                }
                resetClick();
            }
            case ConfigParameters.SAVE_BUTTON -> saveClick();
            case ConfigParameters.LOAD_BUTTON -> {
                if (isOnlineGame) {
                    EmergentPanels.informPlayer(view, "You can't do this on an online game!", "You can't load a saved game during an online game.");
                    return;
                }
                loadClick();
            }
            case ConfigParameters.BACK_BUTTON -> backClick();
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