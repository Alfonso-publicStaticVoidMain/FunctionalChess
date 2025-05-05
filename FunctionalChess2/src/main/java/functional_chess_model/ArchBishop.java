package functional_chess_model;

import graphic_resources.ChessImages;
import java.io.Serializable;
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
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        if (Chess.isKnightLikePath(Xmovement, Ymovement)) return true;
        if (!Chess.isBishopLikePath(Xmovement, Ymovement)) return false;
        return game.isPathClear(initPos.x(), initPos.y(), Xmovement, Ymovement);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new ArchBishop(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITEARCHBISHOP : ChessImages.BLACKARCHBISHOP;
    }
    
}
