package controller;

import configparams.ConfigParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;

import controller.online.NetworkController;
import functional_chess_model.ChessColor;
import functional_chess_model.GameVariant;
import view.Index;
import view.online.ConnectionLogger;

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
            GameType gameType = view.gameTypeSelected();
            ChessController controller = GameVariant.valueOf(command).controller(view.isTimerToggled(), gameType.isOnlineGame(), switch (gameType) {
                case HOST -> ChessColor.WHITE;
                case CLIENT -> ChessColor.BLACK;
                default -> null;
            });
            SwingUtilities.invokeLater(() -> {
                view.dispose();
                if (gameType == GameType.HOST) new NetworkController(controller, new ConnectionLogger()).startServer();
                else if (gameType == GameType.CLIENT) new NetworkController(controller, new ConnectionLogger()).startClient();
            });
            return;
        }

        switch (command) {
            case ConfigParameters.NEW_PIECES_BUTTON -> SwingUtilities.invokeLater(() -> {
                view.dispose();
                new NewPiecesController();
            });
            case ConfigParameters.EXIT_BUTTON -> SwingUtilities.invokeLater(view::dispose);
        }
    }

    public enum GameType {
        HOST,
        CLIENT,
        LOCAL;

        public boolean isOnlineGame() {
            return this != LOCAL;
        }
    }
}
