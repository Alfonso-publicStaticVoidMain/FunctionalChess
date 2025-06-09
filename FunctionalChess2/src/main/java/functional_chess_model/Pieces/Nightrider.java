package functional_chess_model.Pieces;

import functional_chess_model.*;
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
    public boolean canMove(Chess game, Movement move) {
        Position initPos = getPosition();
        if (!move.init().equals(getPosition())) return false;
        int dx = move.dx();
        int dy = move.dy();
        if (dx != 2*dy && dy != 2*dx) return false;
        int elementalDx = dx % 2 == 0 ? 2 : 1;
        int elementalDy = dy % 2 == 0 ? 2 : 1;
        int steps = dx / elementalDx;
        return IntStream.range(1, steps)
            .mapToObj(n -> Position.of(initPos.x() + n * elementalDx, initPos.y()+ n * elementalDy))
            .noneMatch(game::checkPieceAt);
    }

    @Override
    public Piece moveTo(Position finPos) {
        return new Nightrider(finPos, this.getColor());
    }
    
    @Override
    public ImageIcon toIcon() {
        return this.getColor() == ChessColor.WHITE ? ChessImages.WHITE_NIGHTRIDER : ChessImages.BLACK_NIGHTRIDER;
    }
    
}