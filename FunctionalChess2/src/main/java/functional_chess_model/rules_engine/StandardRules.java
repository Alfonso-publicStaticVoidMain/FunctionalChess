package functional_chess_model.rules_engine;

import functional_chess_model.*;
import functional_chess_model.Pieces.King;
import functional_chess_model.Pieces.Pawn;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public record StandardRules(GameVariant variant) implements RulesEngine {

    @Override
    public Optional<Piece> pieceCapturedByMove(Chess game, Piece piece, Position finPos) {
        Optional<Piece> pieceOrNot = game.findPieceAt(finPos);
        if (pieceOrNot.isPresent()) return pieceOrNot;

        if (piece instanceof Pawn) {
            OptionalInt enPassantXDir = getEnPassantXDir(game, piece);
            if (enPassantXDir.isPresent() && enPassantXDir.getAsInt() == Position.xDist(piece.getPosition(), finPos))
                return Optional.of(game.getLastPlay().get().piece());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Piece> pieceCapturedByMove(Chess game, Position initPos, Position finPos) {
        return game.findPieceThenApply(initPos, piece -> pieceCapturedByMove(game, piece, finPos));
    }

    @Override
    public Optional<CastlingType> castlingTypeOfPlay(Chess game, Piece piece, Position finPos) {
        if (!(piece instanceof King)) return Optional.empty();
        if (!finPos.equals(variant.castlingKingPos(CastlingType.LEFT, piece.getColor())) && !finPos.equals(variant.castlingKingPos(CastlingType.RIGHT, piece.getColor())))
            return Optional.empty();
        ChessColor color = piece.getColor();
        int initRow = variant.initRow(color);

        if (game.isCastlingAvailable(color, CastlingType.LEFT) && finPos.equals(variant.castlingKingPos(CastlingType.LEFT, color))) {
            // Checks if there are pieces in the middle of the initial and castling positions
            if (IntStream.rangeClosed(variant.initRookCol(CastlingType.LEFT) + 1, variant.castlingRookCol(CastlingType.LEFT))
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
            if (IntStream.rangeClosed(variant.castlingRookCol(CastlingType.RIGHT), variant.initRookCol(CastlingType.RIGHT) - 1)
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
    }

    @Override
    public Optional<CastlingType> castlingTypeOfPlay(Chess game, Position initPos, Position finPos) {
        return game.findPieceThenApply(initPos, piece -> castlingTypeOfPlay(game, piece, finPos));
    }

    @Override
    public boolean isPlayerInCheck(Chess game, ChessColor color) {
        return game.findRoyalPiece(color).filter(royalPiece -> game.pieces().stream()
            .anyMatch(piece -> piece.getColor() != color && piece.canMove(game, royalPiece.getPosition())))
            .isPresent();
    }

    @Override
    public boolean doesThisMovementCauseACheck(Chess game, Piece piece, Position finPos) {
        return game.tryToMove(piece.getPosition(), finPos, false, this).map(chess -> isPlayerInCheck(chess, piece.getColor())).orElse(false);
    }

    @Override
    public boolean doesThisMovementCauseACheck(Chess game, Position initPos, Position finPos) {
        return game.findPieceThenTest(initPos, piece -> doesThisMovementCauseACheck(game, piece, finPos));
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
        return false;
    }

    @Override
    public boolean isValidMove(Chess game, Piece piece, Position finPos, boolean checkCheck) {
        Position initPos = piece.getPosition();
        Movement move = Movement.of(initPos, finPos);
        if (!basicLegalityChecks(game, piece, finPos, checkCheck)) return false;
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
            return !(Math.abs(dy) == 2 && game.checkPieceAt(Position.of(initPos.x(), initPos.y() + piece.getColor().yDirection())))
                    && (Math.abs(dy) <= 1 || initPos.y() == variant.initRowPawn(piece.getColor()));
        }
        return false;
    }

    /**
     * Performs some common legality checks for all pieces.
     *
     * @param game       The {@link Chess} game the piece is moving within.
     * @param piece      {@link Piece} that is moving.
     * @param finPos     {@link Position} the piece is moving to.
     * @param checkCheck State parameter to track whether we will declare
     *                   a movement illegal if it causes a check.
     * @return False if either of the following happens:
     * <ul>
     * <li>There is a {@link Piece} of the same color on the final position.</li>
     * <li>{@code checkCheck} is true and the game state after performing the
     * movement has the moving player in check.</li>
     * <li>The initial position is the same as the final position.</li>
     * </ul>
     */
    public boolean basicLegalityChecks(Chess game, Piece piece, Position finPos, boolean checkCheck) {
        return !(
                piece.getPosition() == finPos
                        || game.checkPieceSameColorAs(finPos, piece.getColor())
                        || (checkCheck && doesThisMovementCauseACheck(game, piece, finPos))
        );
    }

}
