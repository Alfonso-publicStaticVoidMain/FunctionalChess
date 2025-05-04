package controller;

import functional_chess_model.Chess;
import functional_chess_model.ChessColor;
import functional_chess_model.GameState;
import functional_chess_model.Pawn;
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
    private ChessGUI view;
    /**
     * {@link Piece} currently stored as the potential piece to move.
     */
    private Optional<Position> selectedPosition;

    /**
     * Standard constructor for the {@code ChessController} class, setting the
     * {@link Chess} game, its {@link ChessGUI} view and setting itself as the
     * controller attribute of that view, then initializing its board by 
     * giving its buttons the appropiate actionLister and finally updating the
     * board.
     * @param game
     * @param view 
     */
    public ChessController(Chess game, ChessGUI view) {
        this.game = game;
        this.view = view;
        this.view.setController(this);
        this.view.updateBoard();
        this.selectedPosition = Optional.empty();
    }

    /**
     * Getter for the game attribute of the controller.
     * @return The {@link Chess} game the controller is controlling.
     */
    public Chess getGame() {return game;}
    
    /**
     * Method intended to be used as the action listener for the view's buttons
     * on the chess board.
     * @param x X coordinate of the button clicked.
     * @param y Y coordinate of the button clicked.
     */
    public void handleClick(int x, int y) {
        view.clearHighlights();
        if (x == 0 || y == 0) return; // Ignore label clicks
        if (game.state().hasEnded()) return; // Don't do anything if the game has ended.
        Position clickedPos = Position.of(x, y);
        ChessColor activePlayer = game.activePlayer();
        if (selectedPosition.isEmpty()) { // First click stores the selected piece.
            if (game.checkPieceAt(clickedPos)) {
                Piece piece = game.findPieceAt(clickedPos).get();
                if (piece.getColor() == activePlayer) {
                    selectedPosition = Optional.of(clickedPos);
                    view.highlightValidMoves(piece);
                } else {
                    view.highlightMovesOfEnemyPiece(piece);
                }
            }
        } else { // Second click attempts to do the movement.
            Piece piece = game.findPieceAt(selectedPosition.get()).get();
            if (!piece.isLegalMovement(game, clickedPos)) {
                view.highlightPiecesThatCanCaptureKing(piece, clickedPos);
            }
                
//                if (selectedPiece instanceof King) {
//                    if (game.isCastlingAvailable(game.activePlayer(), CastlingType.LEFT) &&
//                        clickedPos.equals(game.config().kingCastlingPos(game.activePlayer(), CastlingType.LEFT))    
//                    ) {
//                        game.doLeftCastling(game.activePlayer());
//                        playDone = true;
//                        // A play of left castling was done.
//                    }
//                    
//                    if (game.checkRightCastling(game.activePlayer()) &&
//                        clickedPos.equals(game.rightCastlingKingPosition(game.activePlayer()))    
//                    ) {
//                        game.doRightCastling(game.activePlayer());
//                        playDone = true;
//                        // A play of right castling was done.
//                    }
//                }
                
            else {
                Optional<Chess> gameAfterMoveOrNot = game.tryToMove(piece, clickedPos);
                if (gameAfterMoveOrNot.isPresent()) {
                    game = gameAfterMoveOrNot.get();
                    Optional<Play> lastPlay = game.getLastPlay();
                    if (lastPlay.isPresent()) view.updatePlayHistory(lastPlay.get());
                }
            }
            
            if (piece instanceof Pawn && piece.getPosition().y() == game.config().crowningRow(activePlayer)) { // Pawn crowning
                    game = game.crownPawnChain(piece, view.pawnCrowningMenu(game.config().crownablePieces()));
                }
            view.updateActivePlayer();
                
            
            selectedPosition = Optional.empty();
            view.updateBoard();
            
            game = game.checkMateChain(activePlayer);
            if (game.state() == GameState.WHITE_WINS || game.state() == GameState.BLACK_WINS) {
                view.checkMessage(activePlayer);
            } else if (game.state() == GameState.DRAW) {
                view.drawMessage(activePlayer);
            }
        }
    }
    
    public void resetClick() {
        boolean userVerification = view.areYouSureYouWantToDoThis("Do you want to reset the game?");
        if (!userVerification) return;
        game = switch (game.config().typeOfGame()) {
            /*case "Standard Chess"*/ default -> Chess.standardGame();
//            case "Almost Chess" -> Chess.almostChessGame();
//            case "Capablanca Chess" -> Chess.capablancaGame();
//            case "Gothic Chess" -> Chess.gothicGame();
//            case "Janus Chess" -> Chess.janusGame();
//            case "Modern Chess" -> Chess.modernGame();
//            case "Tutti Frutti Chess" -> Chess.tuttiFruttiGame();
        };
        view.updateBoard();
        view.updateActivePlayer();
        view.resetPlayHistory();
    }
    
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
    
    public void loadClick() {
        boolean userVerification = view.areYouSureYouWantToDoThis("Do you want to load a saved game?");
        if (!userVerification) return;
        try (
            FileInputStream fis = new FileInputStream(view.fileChooser("."+File.separator+"savedgames"));
            BufferedInputStream bufis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bufis))
        {
            Chess chessGame = (Chess) ois.readObject();
            if (chessGame.config().rows() == game.config().rows() && chessGame.config().cols() == game.config().cols()) {
                boolean playerChoice = true;
                if (!chessGame.config().typeOfGame().equals(game.config().typeOfGame())) {
                    playerChoice = view.areYouSureYouWantToDoThis("The game you wanted to load is of type: "+chessGame.config().typeOfGame()+", while you're playing "+game.config().typeOfGame()+
                    "\nBut thankfully they are compatible in size. Do you still want to load that game?");
                }
                if (playerChoice) {
                    game = chessGame;
                    view.updateBoard();
                    view.updateActivePlayer();
                    view.reloadPlayHistory();
                }
            } else {
                view.informPlayer("Incompatible dimensions", "Your selected game is of type "+chessGame.config().typeOfGame()+" ("+chessGame.config().rows()+"x"+chessGame.config().cols()+"), while your current one is "+
                game.config().typeOfGame()+" ("+game.config().rows()+"x"+game.config().cols()+")");
            }
            
        }
        catch (IOException ex) {System.err.println("I/O error: " + ex.getMessage());}
        catch (ClassNotFoundException ex) {System.err.println("Class not found: " + ex.getMessage());}
    }
    
    /**
     * Static method to convert a letter to a number.
     * @param letter Letter to convert.
     * @return The integer number representning its position in the english
     * alphabet.
     * @hidden 
     */
    public static int convertLetterToNumber(char letter) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(letter))+1;
    }
    
    /**
     * Static method to convert a number to a letter.
     * @param num Number to convert to a letter.
     * @return The letter in the number's position in the english alphabet.
     * @hidden 
     */
    public static char convertNumberToLetter(int num) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(num-1);
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
                boolean userVerification = game.state() == GameState.NOT_STARTED ? true :
                view.areYouSureYouWantToDoThis("Do you want to go back to the index?\nYou'll lose the state of the game unless you saved it.");
                if (userVerification) {
                    view.dispose();
                    new IndexController();
                }
            });
        }
    }
}
