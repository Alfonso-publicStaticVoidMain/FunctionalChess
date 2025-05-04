package functional_chess_model;

import graphic_resources.ChessImages;
import java.util.OptionalInt;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Pawn extends Piece {

    public Pawn(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        int Ydirection = this.getColor().yDirection();
        int initRow = game.config().initRowPawn(this.getColor());
        
        if (Ymovement * Ydirection <= 0) return false;
        
        if (game.checkPieceAt(finPos)) {
            return Math.abs(Xmovement) == 1 && Math.abs(Ymovement) == 1;
        } else {
            if (Xmovement != 0) {
                OptionalInt xDirEnPassantOrNot = game.getEnPassantXDir(this);
                if (xDirEnPassantOrNot.isPresent()) {
                    if (initPos.y() == game.getLastPlay().get().finPos().y() && Math.abs(Ymovement) == 1 && Xmovement == -xDirEnPassantOrNot.getAsInt()) return true;
                }
                return false;
            }
            if (Math.abs(Ymovement) > 2) return false;
            
            if (Math.abs(Ymovement) == 2 && game.checkPieceAt(Position.of(initPos.x(), initPos.y()+Ydirection))) return false;
            
            
            if (this.getPosition().y() != initRow && Math.abs(Ymovement) > 1) return false;
            
        }
        return true;
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Pawn(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITEPAWN : ChessImages.BLACKPAWN;
    }
    
    
}
