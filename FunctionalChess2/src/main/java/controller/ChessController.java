package controller;

import functional_chess_model.CastlingType;
import functional_chess_model.Chess;
import functional_chess_model.ChessColor;
import functional_chess_model.GameState;
import functional_chess_model.Pieces.King;
import functional_chess_model.Pieces.Pawn;
import functional_chess_model.Piece;
import functional_chess_model.Play;
import functional_chess_model.Position;
import view.ChessGUI;
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
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

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

    /**
     * Standard constructor for the {@code ChessController} class, setting the
     * {@link Chess} game, its {@link ChessGUI} view and setting itself as the
     * controller attribute of that view, then initializing its board by 
     * giving its buttons the appropiate actionLister and finally updating the
     * board.
     * @param game {@link Chess} game this controller is controlling.
     * @param view  {@link ChessGUI} view this controller is controlling.
     */
    public ChessController(Chess game, ChessGUI view) {
        this.game = game;
        this.view = view;
        this.view.setController(this);
        this.view.updateBoard();
        this.selectedPosition = null;
    }

    /**
     * Getter for the game attribute of the controller.
     * @return The {@link Chess} game the controller is controlling.
     */
    public Chess getGame() {return game;}
    
    /**
     * Part of the action listener for the view's buttons on the chess board.
     * @param x X coordinate of the button clicked.
     * @param y Y coordinate of the button clicked.
     */
    public void handleClick(int x, int y) {
        view.clearHighlights();
        if (x == 0 || y == 0) return; // Ignore label clicks
        if (game.state().hasEnded()) return; // Don't do anything if the game has ended.
        
        Position clickedPos = Position.of(x, y);
        ChessColor activePlayer = game.activePlayer();
        
        if (selectedPosition == null) { // First click stores the selected piece.
            if (game.checkPieceAt(clickedPos)) {
                Piece piece = game.findPieceAt(clickedPos).get();
                if (piece.getColor() == activePlayer) {
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

                piece = game.findPieceAt(clickedPos).orElse(piece);
                if (piece instanceof Pawn && piece.getPosition().y() == game.config().crowningRow(activePlayer)) { // Pawn crowning
                    view.updateBoard();
                    game = game.crownPawnChain(piece, view.pawnCrowningMenu(game.config().crownablePieces()));
                }

                Optional<Play> lastPlay = game.getLastPlay();
                lastPlay.ifPresent(view::updatePlayHistory);
                view.updateBoard();

                view.updateActivePlayer();

                game = game.checkMateChain(activePlayer);
                if (game.state() == GameState.WHITE_WINS || game.state() == GameState.BLACK_WINS) {
                    view.checkMessage(activePlayer);
                } else if (game.state() == GameState.DRAW) {
                    view.drawMessage(activePlayer);
                }
            }

            selectedPosition = null;


        }
    }
    
    /**
     * Part of the action listener for the reset button on the view. It changes
     * the game attribute of the controller to the initial state of the game
     * with the configuration currently being used.
     */
    public void resetClick() {
        boolean userVerification = view.areYouSureYouWantToDoThis("Do you want to reset the game?");
        if (!userVerification) return;
        game = switch (game.config().typeOfGame()) {
            case "Standard Chess" -> Chess.standardGame();
            case "Almost Chess" -> Chess.almostChessGame();
            case "Capablanca Chess" -> Chess.capablancaGame();
            case "Gothic Chess" -> Chess.gothicGame();
            case "Janus Chess" -> Chess.janusGame();
            case "Modern Chess" -> Chess.modernGame();
            case "Tutti Frutti Chess" -> Chess.tuttiFruttiGame();
            default -> null;
        };
        view.updateBoard();
        view.updateActivePlayer();
        view.resetPlayHistory();
    }
    
    /**
     * Shows a menu to ask confirmation from the user, then if they confirm,
     * shows a menu to let them write some text that will be the file name
     * that will be saved in savedgames/[name].dat, containing the information
     * about the current state of the game.
     */
    public void saveClick() {
        boolean userVerification = view.areYouSureYouWantToDoThis("Do you want to save the state of the game?");
        if (!userVerification) return;
        String filePath = view.userTextInputMessage("Enter the name of your game");
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
     * dimensions but of a different type, lets the load happen but still
     * shows a warning message.
     */
    public void loadClick() {
        boolean userVerification = view.areYouSureYouWantToDoThis("Do you want to load a saved game?");
        if (!userVerification) return;
        try (
                FileInputStream fis = new FileInputStream(view.fileChooser("." + File.separator + "savedgames"));
                BufferedInputStream bufis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bufis)) {
            Chess chessGame = (Chess) ois.readObject();
            if (chessGame.config().rows() == game.config().rows() && chessGame.config().cols() == game.config().cols()) {
                boolean playerChoice = true;
                if (!chessGame.config().typeOfGame().equals(game.config().typeOfGame())) {
                    playerChoice = view.areYouSureYouWantToDoThis("The game you wanted to load is of type: " + chessGame.config().typeOfGame() + ", while you're playing " + game.config().typeOfGame() +
                            "\nBut thankfully they are compatible in size. Do you still want to load that game?");
                }
                if (playerChoice) {
                    game = chessGame;
                    view.updateBoard();
                    view.updateActivePlayer();
                    view.reloadPlayHistory();
                }
            } else {
                view.informPlayer("Incompatible dimensions", "Your selected game is of type " + chessGame.config().typeOfGame() + " (" + chessGame.config().rows() + "x" + chessGame.config().cols() + "), while your current one is " +
                        game.config().typeOfGame() + " (" + game.config().rows() + "x" + game.config().cols() + ")");
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
            case "Board Button" -> {
                JButton clickedButton = (JButton) e.getSource();
                int x = (int) clickedButton.getClientProperty("x");
                int y = (int) clickedButton.getClientProperty("y");
                System.out.println("[DEBUG] Position: "+Position.of(x, y)+" (x="+x+", y="+y+")");
                handleClick(x, y);
            }
            case "Reset" -> resetClick();
            case "Save" -> saveClick();
            case "Load" -> loadClick();
            case "Back" -> SwingUtilities.invokeLater( () -> {
                boolean userVerification = game.state() == GameState.NOT_STARTED
                        || view.areYouSureYouWantToDoThis("Do you want to go back to the index?\nYou'll lose the state of the game unless you saved it.");
                if (userVerification) {
                    view.dispose();
                    new IndexController();
                }
            });
        }
    }
}
