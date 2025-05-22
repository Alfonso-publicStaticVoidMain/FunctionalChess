package functional_chess_model;

import functional_chess_model.Pieces.Pawn;

import java.io.Serializable;

/**
 * Enum class representing the two colors of chess: black and white. It has
 * int attributes storing the value of the initial row of the pieces in general
 * and of the pawns in particular of that color, the direction the pawns of 
 * that color move, and the row they must reach in order to be able to crown.
 * @author Alfonso Gallego
 */
public enum ChessColor implements Serializable {

    /**
     * WHITE color of chess.
     */
    WHITE(1),

    /**
     * BLACK color of chess.
     */
    BLACK(-1);
    
    /**
     * Integer representing the direction the pawns of this color moves.
     * 1 represents forwards (for whites) and -1 backwards (for blacks).
     */
    private final int yDirection;
    
    ChessColor(int yDirection) {
        this.yDirection = yDirection;
    }

    /**
     * Getter for the Y direction attribute.
     * @return The direction that {@link Pawn}s of this color move towards, ie,
     * 1 for {@code WHITE} and -1 for {@code BLACK}.
     */
    public int yDirection() {return yDirection;}
    
    /**
     * Returns the opposite color of {@code this}.
     * @return The oppositve of {@code this} color, ie, BLACK if {@code this}
     * is WHITE, and WHITE if {@code this} is BLACK.
     */
    public ChessColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    
}
