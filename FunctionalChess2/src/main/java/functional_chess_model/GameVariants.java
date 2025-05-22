package functional_chess_model;

import controller.ChessController;
import view.ChessGUI;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public enum GameVariants {
    STANDARD(isTimed -> new ChessController(Chess.standardGame(isTimed), new ChessGUI(8, 8, isTimed)), Chess::standardGame),
    ALMOSTCHESS(isTimed -> new ChessController(Chess.almostChessGame(isTimed), new ChessGUI(8, 8, isTimed)), Chess::almostChessGame),
    CAPABLANCA(isTimed -> new ChessController(Chess.capablancaGame(isTimed), new ChessGUI(8, 10, isTimed)), Chess::capablancaGame),
    GOTHIC(isTimed -> new ChessController(Chess.gothicGame(isTimed), new ChessGUI(8, 10, isTimed)), Chess::gothicGame),
    JANUS(isTimed -> new ChessController(Chess.janusGame(isTimed), new ChessGUI(8, 10, isTimed)), Chess::janusGame),
    MODERN(isTimed -> new ChessController(Chess.modernGame(isTimed), new ChessGUI(9, 9, isTimed)), Chess::modernGame),
    TUTTIFRUTTI(isTimed -> new ChessController(Chess.tuttiFruttiGame(isTimed), new ChessGUI(8, 8, isTimed)), Chess::tuttiFruttiGame);

    private Consumer<Boolean> controllerGenerator;
    private Function<Boolean, Chess> gameGenerator;
    public Consumer<Boolean> getControllerGenerator() {
        return controllerGenerator;
    }
    public Function<Boolean, Chess> getGameGenerator() {return gameGenerator;}

    GameVariants(Consumer<Boolean> controllerGenerator, Function<Boolean, Chess> gameGenerator) {
        this.controllerGenerator = controllerGenerator;
        this.gameGenerator = gameGenerator;
    }

    public static String[] variantNames() {
        return Stream.of(GameVariants.values())
            .map(Enum::toString)
            .toArray(String[]::new);
    }
}
