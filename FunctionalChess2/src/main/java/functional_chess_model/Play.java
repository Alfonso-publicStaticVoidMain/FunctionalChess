package functional_chess_model;

import java.io.Serializable;
import java.util.Optional;

/**
 * Record representing a Play in a chess game, storing the Piece that was moved,
 * its initial and final Positions, and the Piece that was captured, if any.
 * @author Alfonso Gallego
 * @param piece {@link Piece} that was moved in the Play.
 * @param initPos Initial {@link Position} of the movement.
 * @param finPos Final {@link Position} of the movement.
 * @param pieceCaptured {@link Piece} captured in the Play, if any.
 * @param castlingInfo Integer representing if the {@code Play} represents a
 * castling movement, -1 meaning left castling, 1 right castling and 0 a
 * regular play.
 */
//public class Play {
public record Play(
    Piece piece,
    Position initPos,
    Position finPos,
    Optional<Piece> pieceCaptured,
    Optional<CastlingType> castlingInfo,
    Optional<Piece> pieceCrowned
) implements Serializable {

    /**
     * 4-parameter constructor, setting the pieceCaptured attribute to
     * {@code null}.
     * @param piece {@link Piece} moved.
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     */
    public Play(Piece piece, Position initPos, Position finPos, CastlingType castlingInfo) {
        this(piece, initPos, finPos, Optional.empty(), Optional.of(castlingInfo), Optional.empty());
    }
    
    /**
     * 4-parameter constructor, storing a captured Piece and setting castlingInfo
     * to {@code null}, since capturing is incompatible with castling.
     * @param piece {@link Piece} moved.
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     * @param pieceCaptured {@link Piece} captured.
     */
    public Play(Piece piece, Position initPos, Position finPos, Piece pieceCaptured) {
        this(piece, initPos, finPos, Optional.of(pieceCaptured), Optional.empty(), Optional.empty());
    }
    
    /**
     * 4-parameter constructor, storing a captured Piece and setting castlingInfo
     * to {@code null}, since capturing is incompatible with castling.
     * @param piece {@link Piece} moved.
     * @param initPos Initial {@link Position}.
     * @param finPos Final {@link Position}.
     * @param pieceCaptured {@link Piece} captured wrapped in an Optional monad.
     */
    public Play(Piece piece, Position initPos, Position finPos, Optional<Piece> pieceCaptured) {
        this(piece, initPos, finPos, pieceCaptured, Optional.empty(), Optional.empty());
    }
    
    public Play(Piece piece, Position initPos, Position finPos, Optional<Piece> pieceCaptured, Piece pieceCrowned) {
        this(piece, initPos, finPos, pieceCaptured, Optional.empty(), Optional.of(pieceCrowned));
    }
    
}
