package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Rook extends Piece {

    public Rook(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        return move.init() == getPosition() && move.isStraight() && game.isPathClear(move);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Rook(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_ROOK : ChessImages.BLACK_ROOK;
    }
    
}