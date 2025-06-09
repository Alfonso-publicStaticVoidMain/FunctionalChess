package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class King extends Piece {

    public King(Position position, ChessColor color) {
        super(position, color, true);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        return move.init().equals(getPosition()) && Math.abs(move.dx()) <= 1 && Math.abs(move.dy()) <= 1;
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new King(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_KING : ChessImages.BLACK_KING;
    }
    
}