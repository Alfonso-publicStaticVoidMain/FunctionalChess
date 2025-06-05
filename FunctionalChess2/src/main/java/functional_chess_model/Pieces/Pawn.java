package functional_chess_model.Pieces;

import functional_chess_model.*;
import graphic_resources.ChessImages;
import java.util.OptionalInt;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Pawn extends Piece {

    //TODO Decouple rules's en passant checks from Pawn's inherent movement logic

    public Pawn(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean canMove(Chess game, Movement move) {
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, move);
        int Ymovement = Position.yDist(initPos, move);
        int Ydirection = this.getColor().yDirection();
        int initRow = game.variant().initRowPawn(this.getColor());
        
        if (Ymovement * Ydirection <= 0) return false;
        
        if (game.checkPieceAt(move)) {
            return Math.abs(Xmovement) == 1 && Math.abs(Ymovement) == 1;
        } else {
            if (Xmovement != 0) {
                OptionalInt xDirEnPassantOrNot = game.getEnPassantXDir(this);
                if (xDirEnPassantOrNot.isPresent()) {
                    return initPos.y() == game.getLastPlay().get().finPos().y() && Math.abs(Ymovement) == 1 && Xmovement == xDirEnPassantOrNot.getAsInt();
                }
                return false;
            }
            if (Math.abs(Ymovement) > 2) return false;
            
            if (Math.abs(Ymovement) == 2 && game.checkPieceAt(Position.of(initPos.x(), initPos.y()+Ydirection))) return false;

            return this.getPosition().y() == initRow || Math.abs(Ymovement) <= 1;
            
        }
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Pawn(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_PAWN : ChessImages.BLACK_PAWN;
    }
    
}