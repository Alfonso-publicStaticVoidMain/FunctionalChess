package controller;


import configparams.ConfigParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

import controller.online.ClientController;
import controller.online.ServerController;
import functional_chess_model.ChessColor;
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

        if (ConfigParameters.variantEnumNames.contains(command)) {
            String mode = view.getNetworkMode();
            ChessController controller = GameVariant.valueOf(command).controller(isTimed, true, mode.equals("HOST") ? ChessColor.WHITE : ChessColor.BLACK);
            SwingUtilities.invokeLater(() -> {
                view.dispose();
                switch (mode) {
                    case "HOST" -> {ServerController.startServer(controller);}
                    case "CLIENT" -> {ClientController.startClient(controller);}
                }
            });
            return;
        }

        switch (command) {
            case ConfigParameters.NEW_PIECES_BUTTON -> SwingUtilities.invokeLater(() -> {
                    view.dispose();
                    new NewPiecesController();
            });
            case ConfigParameters.TIMER_TOGGLE -> isTimed = !isTimed;
            case ConfigParameters.EXIT_BUTTON -> SwingUtilities.invokeLater(view::dispose);
        }
    }    
}
