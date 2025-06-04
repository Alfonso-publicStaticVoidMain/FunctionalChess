package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

import configparams.ConfigParameters;
import view.NewPieces;

/**
 *
 * @author Alfonso Gallego
 */
public class NewPiecesController implements ActionListener {
    
    private final NewPieces view;
    
    public NewPiecesController() {
        this.view = new NewPieces();
        this.view.setController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] NewPiecesController action received: "+command);
        if (command.equals(ConfigParameters.BACK_BUTTON)) {
            SwingUtilities.invokeLater(() -> {
                view.dispose();
                new IndexController();
            });
        }
    }
}
