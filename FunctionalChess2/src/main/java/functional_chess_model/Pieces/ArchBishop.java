package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class ArchBishop extends Piece {

    public ArchBishop(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        return move.init() == getPosition()
            && (move.isKnightLike() || (move.isDiagonal() && game.isPathClear(move)));
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new ArchBishop(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_ARCHBISHOP : ChessImages.BLACK_ARCHBISHOP;
    }
    
}