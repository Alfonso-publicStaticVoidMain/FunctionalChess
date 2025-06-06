package functional_chess_model;

import functional_chess_model.Pieces.*;

import java.util.function.BiFunction;

/**
 * Enum class listing all {@link Piece} classes implemented thus far.
 * Each instance of this enum has an attribute being the constructor
 * for Pieces of that variant, a Function&lt;Position, ChessColor, Piece&gt;.
 * Royalty of the Piece is assumed by each class's 2-parameter constructor
 * as needed.
 */
public enum PieceType {
    AMAZON(Amazon::new),
    ARCHBISHOP(ArchBishop::new),
    BISHOP(Bishop::new),
    CHANCELLOR(Chancellor::new),
    KING(King::new),
    KNIGHT(Knight::new),
    NIGHTRIDER(Nightrider::new),
    PAWN(Pawn::new),
    QUEEN(Queen::new),
    ROOK(Rook::new);

    private final BiFunction<Position, ChessColor, Piece> constructor;

    public Piece constructor(Position pos, ChessColor color) {
        return constructor.apply(pos, color);
    }

    PieceType(BiFunction<Position, ChessColor, Piece> constructor) {this.constructor = constructor;}
}