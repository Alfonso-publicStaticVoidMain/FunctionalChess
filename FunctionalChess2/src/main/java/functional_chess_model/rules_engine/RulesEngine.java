package functional_chess_model.rules_engine;

import functional_chess_model.*;
import functional_chess_model.Pieces.King;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public interface RulesEngine {

    GameVariant variant();

    Optional<Piece> pieceCapturedByMove(Chess game, Piece piece, Position finPos);

    Optional<Piece> pieceCapturedByMove(Chess game, Position initPos, Position finPos);

    Optional<CastlingType> castlingTypeOfPlay(Chess game, Piece piece, Position finPos);

    Optional<CastlingType> castlingTypeOfPlay(Chess game, Position initPos, Position finPos);

    boolean isPlayerInCheck(Chess game, ChessColor color);

    boolean doesThisMovementCauseACheck(Chess game, Piece piece, Position finPos);

    boolean doesThisMovementCauseACheck(Chess game, Position initPos, Position finPos);

    OptionalInt getEnPassantXDir(Chess game, Piece piece);

    boolean isValidMove(Chess game, Movement move, boolean checkCheck);

    default boolean isValidMove(Chess game, Movement move) {
        return isValidMove(game, move, true);
    }

    default boolean isValidMove(Chess game, Position initPos, Position finPos, boolean checkCheck) {
        return isValidMove(game, Movement.of(initPos, finPos), checkCheck);
    }

    default boolean isValidMove(Chess game, Position initPos, Position finPos) {
        return isValidMove(game, Movement.of(initPos, finPos), true);
    }

    boolean isValidMove(Chess game, Piece piece, Position finPos, boolean checkCheck);

    default boolean isValidMove(Chess game, Piece piece, Position finPos) {
        return isValidMove(game, piece, finPos, true);
    }

    default List<Position> validMovesOf(Chess game, Piece piece) {
        return variant().positionsThatValidate(pos -> isValidMove(game, piece, pos) ||
            (piece instanceof King && castlingTypeOfPlay(game, piece, pos).isPresent()));
    }

    default List<Position> validMovesThatWouldCauseCheckOf(Chess game, Piece piece) {
        return variant().positionsThatValidate(pos -> isValidMove(game, piece, pos, false) && !isValidMove(game, piece, pos, true));
    }

    default List<Position> piecesThatCanCaptureKing(Chess game, Piece piece, Position finPos) {
        Chess gameAfterMovement = game.tryToMoveChain(piece, finPos, false, this);
        ChessColor color = piece.getColor();
        Optional<Piece> royalPieceOrNot = gameAfterMovement.findRoyalPiece(color);
        if (royalPieceOrNot.isEmpty()) return List.of();

        return gameAfterMovement.pieces().stream()
            .filter(p -> // Filter for the initPieces of a different color than active player that can move to capture active player's King.
                p.getColor() != color &&
                    p.canMove(gameAfterMovement, royalPieceOrNot.get().getPosition())
            )
            .map(Piece::getPosition)
            .toList();
    }

    RulesEngine STANDARD_RULES = new StandardRules(GameVariant.STANDARD);
    RulesEngine ALMOST_CHESS_RULES = new StandardRules(GameVariant.ALMOSTCHESS);
    RulesEngine CAPABLANCA_RULES = new StandardRules(GameVariant.CAPABLANCA);
    RulesEngine GOTHIC_RULES = new StandardRules(GameVariant.GOTHIC);
    RulesEngine JANUS_RULES = new StandardRules(GameVariant.JANUS);
    RulesEngine MODERN_RULES = new StandardRules(GameVariant.MODERN);
    RulesEngine TUTTI_FRUTTI_RULES = new StandardRules(GameVariant.TUTTIFRUTTI);

}
