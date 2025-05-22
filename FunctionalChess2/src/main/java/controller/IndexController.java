package controller;


import configparams.ConfigParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.SwingUtilities;

import functional_chess_model.GameVariant;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] IndexController action received: "+command);

        if (ConfigParameters.variantNamesUpperCase.contains(command)) {
            SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    GameVariant.valueOf(command).controller(isTimed);
                }
            );
        }

        switch (command) {
            case ConfigParameters.newPiecesButtonActionCommand -> {
                SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    new NewPiecesController();
                });
            }
            case ConfigParameters.timerToggleActionCommand -> {
                isTimed = !isTimed;
            }
            case ConfigParameters.exitButtonActionCommand -> {
                SwingUtilities.invokeLater(view::dispose);
            }
        }
    }    
}
