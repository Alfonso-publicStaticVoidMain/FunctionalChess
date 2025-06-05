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
public class Chancellor extends Piece {

    public Chancellor(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMoveTo(Chess game, Position finPos) {
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        if (Chess.isKnightLikePath(Xmovement, Ymovement)) return true;
        if (!Chess.isRookLikePath(Xmovement, Ymovement)) return false;
        return game.isPathClear(initPos.x(), initPos.y(), Xmovement, Ymovement);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Chancellor(finPos, this.getColor());
    }
 
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_CHANCELLOR : ChessImages.BLACK_CHANCELLOR;
    }
    
}