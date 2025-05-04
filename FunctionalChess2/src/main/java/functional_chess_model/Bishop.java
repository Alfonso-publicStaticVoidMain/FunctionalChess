package functional_chess_model;

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
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        if (!Chess.isBishopLikePath(Xmovement, Ymovement)) return false;
        return game.isPathClear(initPos.x(), initPos.y(), Xmovement, Ymovement);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Bishop(finPos, this.getColor());
    }
 
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITEBISHOP : ChessImages.BLACKBISHOP;
    }
    
}
