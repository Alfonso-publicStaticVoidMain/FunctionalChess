package main;

import controller.IndexController;
import javax.swing.SwingUtilities;

/**
 * Main class to play Chess via a GUI.
 * @author Alfonso Gallego Fernández
 */
public class ChessMainGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IndexController::new);
    }
    
}
