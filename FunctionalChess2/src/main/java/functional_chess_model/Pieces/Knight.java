package functional_chess_model.Pieces;

import functional_chess_model.Chess;
import functional_chess_model.ChessColor;
import functional_chess_model.Piece;
import functional_chess_model.Position;
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
    public boolean canMoveTo(Chess game, Position finPos) {
        return Chess.isKnightLikePath(this.getPosition(), finPos);
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