package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Knight extends Piece {

    public Knight(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        return move.init().equals(getPosition()) && move.isKnightLike();
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Knight(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_KNIGHT : ChessImages.BLACK_KNIGHT;
    }
    
}