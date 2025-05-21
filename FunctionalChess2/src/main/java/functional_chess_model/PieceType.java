package functional_chess_model;

import java.util.function.BiFunction;

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

    public BiFunction<Position, ChessColor, Piece> constructor() {return constructor;}

    PieceType(BiFunction<Position, ChessColor, Piece> constructor) {this.constructor = constructor;}
}
