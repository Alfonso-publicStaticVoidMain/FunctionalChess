package controller;


import functional_chess_model.Chess;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import view.ChessGUI;
import view.Index;

/**
 *
 * @author Alfonso Gallego
 */
public class IndexController implements ActionListener {

    private final Index view;

    public IndexController() {
        this.view = new Index();
        this.view.setController(this);
    }

    public static void initializeStandardGame() {
        new ChessController(Chess.standardGame(), new ChessGUI(8, 8));
    }
    
    public static void initializeAlmostChessGame() {
        new ChessController(Chess.almostChessGame(), new ChessGUI(8, 8));
    }
    
    public static void initializeCapablancaGame() {
        new ChessController(Chess.capablancaGame(), new ChessGUI(8, 10));
    }
    
    public static void initializeGothicGame() {
        new ChessController(Chess.gothicGame(), new ChessGUI(8, 10));
    }
    
    public static void initializeJanusGame() {
        new ChessController(Chess.janusGame(), new ChessGUI(8, 10));
    }
    
    public static void initializeModernGame() {
        new ChessController(Chess.modernGame(), new ChessGUI(9, 9));
    }

    public static void initializeTuttiFruttiGame() {
        new ChessController(Chess.tuttiFruttiGame(), new ChessGUI(8, 8));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] IndexController action received: "+command);
        switch (command) {
            case "Standard Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeStandardGame();
                });
            }
            case "Almost Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeAlmostChessGame();
                });
            }
            case "Capablanca Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeCapablancaGame();
                });
            }
            case "Gothic Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeGothicGame();
                });
            }
            case "Janus Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeJanusGame();
                });
            }
            case "Modern Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeModernGame();
                });
            }
            case "Tutti Frutti Chess" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    initializeTuttiFruttiGame();
                });
            }
            case "New Pieces" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    new NewPiecesController();
                });
            }
            case "Exit" -> {
                SwingUtilities.invokeLater(view::dispose);
            }
        }
    }    
}
