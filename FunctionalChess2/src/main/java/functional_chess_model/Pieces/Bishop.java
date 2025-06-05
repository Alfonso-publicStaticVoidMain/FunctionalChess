package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Bishop extends Piece {

    public Bishop(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        return move.init() == getPosition() && move.isDiagonal() && game.isPathClear(move);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Bishop(finPos, this.getColor());
    }
 
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_BISHOP : ChessImages.BLACK_BISHOP;
    }
    
}