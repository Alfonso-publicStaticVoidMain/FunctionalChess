package functional_chess_model.rules_engine;

import functional_chess_model.*;
import functional_chess_model.Pieces.King;
import functional_chess_model.Pieces.Pawn;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class StandardRules implements RulesEngine {

    private final GameVariant variant;

    public StandardRules(GameVariant variant) {this.variant = variant;}

    @Override
    public GameVariant variant() {
        return variant;
    }

    @Override
    public Optional<Piece> pieceCapturedByMove(Chess game, Movement move) {
        return game.findPieceThenApply(move.init(), piece -> {
            Optional<Piece> pieceCapturedOrNot = game.findPieceAt(move.fin());
            if (pieceCapturedOrNot.isPresent()) return pieceCapturedOrNot;

            if (piece instanceof Pawn) {
                OptionalInt enPassantXDir = getEnPassantXDir(game, piece);
                if (enPassantXDir.isPresent() && enPassantXDir.getAsInt() == move.dx()) return Optional.of(game.getLastPlay().get().piece());
            }
            return Optional.empty();
        });
    }

    @Override
    public Optional<CastlingType> castlingTypeOfPlay(Chess game, Movement move) {
        return game.findPieceThenApply(move.init(), piece -> {
            Position finPos = move.fin();
            if (!(piece instanceof King)) return Optional.empty();
            if (!finPos.equals(variant.castlingKingPos(CastlingType.LEFT, piece.getColor())) && !finPos.equals(variant.castlingKingPos(CastlingType.RIGHT, piece.getColor()))) return Optional.empty();
            ChessColor color = piece.getColor();
            int initRow = variant.initRow(color);

            if (game.isCastlingAvailable(color, CastlingType.LEFT) && finPos.equals(variant.castlingKingPos(CastlingType.LEFT, color))) {
                // Checks if there are pieces in the middle of the initial and castling positions
                if (IntStream.rangeClosed(variant.initRookCol(CastlingType.LEFT)+1, variant.castlingRookCol(CastlingType.LEFT))
                    .anyMatch(x -> game.checkPieceAt(Position.of(x, initRow)))) return Optional.empty();

                // Checks if any piece could threaten to capture the King if it were on the middle positions.
                if (IntStream.rangeClosed(variant.castlingKingCol(CastlingType.LEFT), variant.kingInitCol())
                    .anyMatch(x -> game.pieces().stream()
                        .anyMatch(p -> p.getColor() != color && (
                            p.canMove(game, Position.of(x, initRow))
                                || (p instanceof Pawn &&
                                Math.abs(Position.yDist(p.getPosition(), Position.of(x, initRow))) == 1 &&
                                Math.abs(Position.xDist(p.getPosition(), Position.of(x, initRow))) == 1
                            ))
                        ))) return Optional.empty();
                return Optional.of(CastlingType.LEFT);
            }

            if (game.isCastlingAvailable(color, CastlingType.RIGHT) && finPos.equals(variant.castlingKingPos(CastlingType.RIGHT, color))) {
                // Checks if there are pieces in the middle of the initial and castling positions
                if (IntStream.rangeClosed(variant.castlingRookCol(CastlingType.RIGHT), variant.initRookCol(CastlingType.RIGHT)-1)
                    .anyMatch(x -> game.checkPieceAt(Position.of(x, initRow)))) return Optional.empty();

                // Checks if any piece could threaten to capture the King if it were on the middle positions.
                if (IntStream.rangeClosed(variant.kingInitCol(), variant.castlingKingCol(CastlingType.RIGHT))
                    .anyMatch(x -> game.pieces().stream()
                        .anyMatch(p -> p.getColor() != color && (
                            p.canMove(game, Position.of(x, initRow))
                                || (p instanceof Pawn &&
                                Math.abs(Position.yDist(p.getPosition(), Position.of(x, initRow))) == 1 &&
                                Math.abs(Position.xDist(p.getPosition(), Position.of(x, initRow))) == 1
                            ))
                        ))) return Optional.empty();
                return Optional.of(CastlingType.RIGHT);
            }

            // If neither castling was available or the position wasn't the expected for that variant, returns empty.
            return Optional.empty();
        });
    }

    @Override
    public boolean isPlayerInCheck(Chess game, ChessColor color) {
        return game.findRoyalPiece(color).filter(royalPiece -> game.pieces().stream()
            .anyMatch(piece -> piece.getColor() != color && piece.canMove(game, royalPiece.getPosition())))
            .isPresent();
    }

    @Override
    public boolean doesThisMovementCauseACheck(Chess game, Movement move) {
        return game.findPieceThenTest(move.init(), piece -> game.tryToMove(piece.getPosition(), move.fin(), false, this).map(chess -> isPlayerInCheck(chess, piece.getColor())).orElse(false));
    }

    @Override
    public OptionalInt getEnPassantXDir(Chess game, Piece piece) {
        Optional<Play> lastPlayOrNot = game.getLastPlay();

        if (lastPlayOrNot.isEmpty()) return OptionalInt.empty();

        Play lastPlay = lastPlayOrNot.get();
        Piece lastPieceMoved = lastPlay.piece();

        if (!(lastPieceMoved instanceof Pawn) || !(piece instanceof Pawn)) return OptionalInt.empty();
        if (lastPieceMoved.getColor() == piece.getColor()) return OptionalInt.empty();
        if (Math.abs(Position.yDist(lastPlay.initPos(), lastPlay.finPos())) != 2) return OptionalInt.empty();
        if (Math.abs(Position.xDist(lastPlay.finPos(), piece.getPosition())) != 1) return OptionalInt.empty();

        return OptionalInt.of(Position.xDist(piece.getPosition(), lastPlay.finPos()));
    }

    @Override
    public boolean isValidMove(Chess game, Movement move, boolean checkCheck) {
        Position initPos = move.init();
        return game.findPieceThenTest(initPos, piece -> {
            if (!basicLegalityChecks(game, move, checkCheck)) return false;
            if (piece.canMove(game, move)) return true;
            if (piece instanceof Pawn) {
                int dx = move.dx();
                int dy = move.dy();
                if (dx != 0) {
                    OptionalInt xDirEnPassantOrNot = getEnPassantXDir(game, piece);
                    return xDirEnPassantOrNot.isPresent()
                        && initPos.y() == game.getLastPlay().get().finPos().y()
                        && Math.abs(dy) == 1 && dx == xDirEnPassantOrNot.getAsInt();
                }
                return !(Math.abs(dy) == 2 && game.checkPieceAt(Position.of(initPos.x(), initPos.y()+piece.getColor().yDirection())))
                        && (Math.abs(dy) <= 1 || piece.getPosition().y() == variant.initRowPawn(piece.getColor()));
            }
            return false;
        });
    }

    /**
     * Performs some common legality checks for all pieces.
     * @param game The {@link Chess} game the piece is moving within.
     * @param move {@link Movement} being performed.
     * @param checkCheck State parameter to track whether we will declare
     * a movement illegal if it causes a check.
     * @return False if either of the following happens:
     * <ul>
     * <li>There is a {@link Piece} of the same color on the final position.</li>
     * <li>{@code checkCheck} is true and the game state after performing the
     * movement has the moving player in check.</li>
     * <li>The initial position is the same as the final position.</li>
     * </ul>
     */
    public boolean basicLegalityChecks(Chess game, Movement move, boolean checkCheck) {
        return game.findPieceThenTest(move.init(), piece -> !(
            move.isNull()
                || game.checkPieceSameColorAs(move.fin(), piece.getColor())
                || (checkCheck && doesThisMovementCauseACheck(game, move))
        ));
    }

}
