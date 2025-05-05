package functional_chess_model;

import graphic_resources.ChessImages;
import java.io.Serializable;
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
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        return Chess.isKnightLikePath(this.getPosition(), finPos);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Knight(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITEKNIGHT : ChessImages.BLACKKNIGHT;
    }
    
}
