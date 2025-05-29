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

    public IndexController() {
        this.view = new Index();
        this.view.setController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        System.out.println("[DEBUG] IndexController action received: "+command);

        if (ConfigParameters.VARIANT_ENUM_NAMES.contains(command)) {
            boolean hostingSelected = view.isHostingSelected();
            ChessController controller = GameVariant.valueOf(command).controller(view.isTimerToggled(), hostingSelected, ChessColor.WHITE);
            SwingUtilities.invokeLater(() -> {
                view.dispose();
                if (hostingSelected) ServerController.startServer(controller);
            });
            return;
        }

        switch (command) {
            case ConfigParameters.NEW_PIECES_BUTTON -> SwingUtilities.invokeLater(() -> {
                view.dispose();
                new NewPiecesController();
            });
            case ConfigParameters.JOIN_BUTTON -> SwingUtilities.invokeLater(() -> {
                view.dispose();
                ClientController.startClient(GameVariant.valueOf(command).controller(view.isTimerToggled(), true, ChessColor.BLACK));
            });
            case ConfigParameters.EXIT_BUTTON -> SwingUtilities.invokeLater(view::dispose);
        }
    }    
}
