package functional_chess_model;

import java.io.Serializable;
import java.util.Objects;
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

    public abstract boolean canMoveTo(Chess game, Position finPos);
    public abstract Piece moveTo(Position finPos);
    public abstract ImageIcon toIcon();
    
    /**
     * Returns String representing {@code this}'s color and variant.
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

    @Override
    public int hashCode() {
        return Objects.hash(position, color, royal);
    }

}