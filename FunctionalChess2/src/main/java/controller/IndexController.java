package controller;


import functional_chess_model.Chess;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import javax.swing.SwingUtilities;

import functional_chess_model.GameVariants;
import view.ChessGUI;
import view.Index;

/**
 *
 * @author Alfonso Gallego
 */
public class IndexController implements ActionListener {

    private final Index view;
    private boolean isTimed = false;

    public IndexController() {
        this.view = new Index();
        this.view.setController(this);
    }
/*
    public void initializeStandardGame() {
        new ChessController(Chess.standardGame(isTimed), new ChessGUI(8, 8, isTimed));
    }
    
    public void initializeAlmostChessGame() {
        new ChessController(Chess.almostChessGame(isTimed), new ChessGUI(8, 8, isTimed));
    }
    
    public void initializeCapablancaGame() {
        new ChessController(Chess.capablancaGame(isTimed), new ChessGUI(8, 10, isTimed));
    }
    
    public void initializeGothicGame() {
        new ChessController(Chess.gothicGame(isTimed), new ChessGUI(8, 10, isTimed));
    }
    
    public void initializeJanusGame() {
        new ChessController(Chess.janusGame(isTimed), new ChessGUI(8, 10, isTimed));
    }
    
    public void initializeModernGame() {
        new ChessController(Chess.modernGame(isTimed), new ChessGUI(9, 9, isTimed));
    }

    public void initializeTuttiFruttiGame() {
        new ChessController(Chess.tuttiFruttiGame(isTimed), new ChessGUI(8, 8, isTimed));
    }
*/
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] IndexController action received: "+command);

        if (Arrays.asList(GameVariants.variantNames()).contains(command)) {
            SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    GameVariants.valueOf(command).getControllerGenerator().accept(isTimed);
                }
            );
        }

        switch (command) {
            /*
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
            */
            case "New Pieces" -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    new NewPiecesController();
                });
            }
            case "timer" -> {
                isTimed = !isTimed;
            }
            case "Exit" -> {
                SwingUtilities.invokeLater(view::dispose);
            }
        }
    }    
}
