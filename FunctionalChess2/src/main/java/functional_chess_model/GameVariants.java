package functional_chess_model;

import controller.ChessController;
import view.ChessGUI;

import java.util.function.Consumer;
import java.util.stream.Stream;

public enum GameVariants {
    STANDARD(isTimed -> new ChessController(Chess.standardGame(isTimed), new ChessGUI(8, 8, isTimed))),
    ALMOSTCHESS(isTimed -> new ChessController(Chess.almostChessGame(isTimed), new ChessGUI(8, 8, isTimed))),
    CAPABLANCA(isTimed -> new ChessController(Chess.capablancaGame(isTimed), new ChessGUI(8, 10, isTimed))),
    GOTHIC(isTimed -> new ChessController(Chess.gothicGame(isTimed), new ChessGUI(8, 10, isTimed))),
    JANUS(isTimed -> new ChessController(Chess.janusGame(isTimed), new ChessGUI(8, 10, isTimed))),
    MODERN(isTimed -> new ChessController(Chess.modernGame(isTimed), new ChessGUI(9, 9, isTimed))),
    TUTTIFRUTTI(isTimed -> new ChessController(Chess.tuttiFruttiGame(isTimed), new ChessGUI(8, 8, isTimed)));

    private Consumer<Boolean> controllerGenerator;

    public Consumer<Boolean> getControllerGenerator() {
        return controllerGenerator;
    }

    GameVariants(Consumer<Boolean> controllerGenerator) {
        this.controllerGenerator = controllerGenerator;
    }

    public static String[] variantNames() {
        return Stream.of(GameVariants.values())
            .map(Enum::toString)
            .toArray(String[]::new);
    }
}
