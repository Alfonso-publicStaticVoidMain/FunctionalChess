package functional_chess_model;

import java.util.Optional;

/**
 * Record representing a Play in a chess game, storing the Piece that was moved,
 * its initial and final Positions, and the Piece that was captured, if any.
 * @author Alfonso Gallego
 * @param piece {@link Piece} that was moved in the Play.
 * @param initPos Initial {@link Position} of the movement.
 * @param finPos Final {@link Position} of the movement.
 * @param pieceCaptured Optional {@link Piece} captured in the Play, if any.
 * @param castlingInfo Optional of the enum class {@link CastlingType}, with
 * values LEFT and RIGHT, representing what type of castling was made, if any.
 * @param pieceCrowned Optional {@link Piece} representing the piece the
 * moved piece was crowned into, if any.
 */
public record Play(
    Piece piece,
    Position initPos,
    Position finPos,
    Piece pieceCaptured,
    CastlingType castlingInfo,
    Piece pieceCrowned
) {

    /**
     * 4-parameter constructor, setting the {@code pieceCaptured} and
     * {@code pieceCrowned} attribute to {@code Optional.empty}.
     * @param piece {@link Piece} moved.
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     */
    public Play(Piece piece, Position initPos, Position finPos, CastlingType castlingInfo) {
        this(piece, initPos, finPos, null, castlingInfo, null);
    }
    
    /**
     * 4-parameter constructor, storing a captured Piece and setting 
     * {@code castlingInfo} and {@code pieceCrowned} to {@code Optional.empty},
     * since capturing is incompatible with castling.
     * @param piece {@link Piece} moved.
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     * @param pieceCaptured {@link Piece} captured.
     */
    public Play(Piece piece, Position initPos, Position finPos, Piece pieceCaptured) {
        this(piece, initPos, finPos, pieceCaptured, null, null);
    }
    
    /**
     * 5-parameter constructor, storing the Piece the Pawn was crowned into,
     * and setting castlingInfo to {@code Optional.empty}, since crowning is
     * incompatible with castling.
     * @param piece {@link Piece} moved (when it was a {@link Pawn}).
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     * @param pieceCaptured {@link Piece} captured.
     * @param pieceCrowned {@link Piece} that it was crowned into (with its
     * new type).
     */
    public Play(Piece piece, Position initPos, Position finPos, Piece pieceCaptured, Piece pieceCrowned) {
        this(piece, initPos, finPos, pieceCaptured, null, pieceCrowned);
    }
    
}
