package functional_chess_model;

import java.io.Serializable;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public abstract class Piece implements Serializable {
    private final Position position;
    private final ChessColor color;
    private final boolean royal;

    protected Piece(Position position, ChessColor color) {
        this(position, color, false);
    }
    
    protected Piece(Position position, ChessColor color, boolean isRoyal) {
        this.position = position;
        this.color = color;
        this.royal = isRoyal;
    }

    public Position getPosition() {return position;}
    public ChessColor getColor() {return color;}
    public boolean isRoyal() {return royal;}

    public abstract boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck);
    public boolean isLegalMovement(Chess game, Position finPos) {
        return isLegalMovement(game, finPos, true);
    }
    public abstract Piece moveTo(Position finPos);
    public abstract ImageIcon toIcon();

    /**
     * Performs some common legality checks that will be referenced by each
     * implementation of {@link Piece#isLegalMovement(Chess, Position, boolean)}. 
     * @param game {@link Chess} Game where {@code this} {@link Piece} is
     * moving.
     * @param finPos {@link Position} the piece is moving to.
     * @param checkCheck State parameter to track whether we will declare
     * a movement illegal if it causes a check.
     * @return False if either of the following happens:
     * <ul>
     * <li>There is a {@link Piece} of the same color on the final position.</li>
     * <li>{@code checkCheck} is true and the game state after performing the
     * movement has the moving player in check.</li>
     * <li>The initial position is the same as the final position.</li>
     * </ul>
     */
    public boolean basicLegalityChecks(Chess game, Position finPos, boolean checkCheck) {
        return !(
            position.equals(finPos)
            || game.checkPieceSameColorAs(finPos, color)
            || (checkCheck && game.doesThisMovementCauseACheck(this, finPos))
            
        );
    }
    
    /**
     * Returns String representing {@code this}'s color and type.
     * @return A concatenation of the name of the color of {@code this} Piece,
     * a blank space, and the simple name of {@code this}'s class.
     */
    @Override
    public String toString() {
        return this.getColor() + " " + this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Piece other = (Piece) obj;
        return (this.color == other.color && this.royal == other.royal && this.position.equals(other.position));
    }

}
