package functional_chess_model;

/**
 *
 * @author Alfonso Gallego
 */
public enum GameState {
    NOT_STARTED,
    IN_PROGRESS,
    DRAW,
    WHITE_WINS,
    BLACK_WINS;
    
    public static GameState playerWins(ChessColor color) {
        return color == ChessColor.WHITE ? WHITE_WINS : BLACK_WINS;
    }
    
    public boolean canContinue() {
        return this == NOT_STARTED || this == IN_PROGRESS;
    }
    
    public boolean hasEnded() {
        return this == DRAW || this == WHITE_WINS || this == BLACK_WINS;
    }
}
