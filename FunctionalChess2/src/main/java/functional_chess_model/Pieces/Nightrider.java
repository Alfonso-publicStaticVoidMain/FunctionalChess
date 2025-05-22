package functional_chess_model.Pieces;

import functional_chess_model.Chess;
import functional_chess_model.ChessColor;
import functional_chess_model.Piece;
import functional_chess_model.Position;
import graphic_resources.ChessImages;
import java.util.stream.IntStream;
import javax.swing.ImageIcon;

/**
 *
 * @author Alfonso Gallego
 */
public class Nightrider extends Piece {

    public Nightrider(Position position, ChessColor color) {
        super(position, color);
    }
    
    @Override
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        if (Xmovement != 2*Ymovement && Ymovement != 2*Xmovement) return false;
        int elementalXmov = Xmovement % 2 == 0 ? 2 : 1;
        int elementalYmov = Ymovement % 2 == 0 ? 2 : 1;
        int steps = Xmovement / elementalXmov;
        return IntStream.range(1, steps)
            .mapToObj(n -> Position.of(initPos.x() + n * elementalXmov, initPos.y()+ n * elementalYmov))
            .allMatch(pos -> !game.checkPieceAt(pos));
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Nightrider(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITENIGHTRIDER : ChessImages.BLACKNIGHTRIDER;
    }
    
}