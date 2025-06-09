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
        return move.init().equals(getPosition())
            && move.isForwardFor(getColor())
            && (
                (move.dx() == 0 && (Math.abs(move.dy()) == 1
                || (Math.abs(move.dy()) == 2 && getPosition().y() == game.variant().initRowPawn(getColor()))))
                || (Math.abs(move.dx()) == 1 && Math.abs(move.dy()) == 1 && game.checkPieceAt(move.fin()))
            );
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