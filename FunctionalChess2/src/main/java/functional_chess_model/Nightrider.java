package functional_chess_model;

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
    
    // TO DO - Test and verify nightrider movement
    @Override
    public boolean isLegalMovement(Chess game, Position finPos, boolean checkCheck) {
        if (!basicLegalityChecks(game, finPos, checkCheck)) return false;
        Position initPos = this.getPosition();
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        if (
            (Xmovement % 3 != 0 && Xmovement % 2 != 0) ||
            (Ymovement % 3 != 0 && Ymovement % 2 != 0) ||
            (Xmovement % 3 == 0 && Ymovement % 3 == 0) ||
            (Xmovement % 2 == 0 && Ymovement % 2 == 0)
        ) return false;
        int elementalXmov = Xmovement % 3 == 0 ? 3 : 2;
        int elementalYmov = Ymovement % 3 == 0 ? 3 : 2;
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
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITEKNIGHT : ChessImages.BLACKKNIGHT;
    }
    
}
