package functional_chess_model.rules_engine;

import functional_chess_model.*;

import java.util.Optional;
import java.util.OptionalInt;

public class StandardRules implements RulesEngine {

    private final GameVariant variant;

    public StandardRules(GameVariant variant) {
        this.variant = variant;
    }

    @Override
    public Optional<Piece> pieceCapturedByMove(Chess game, Piece piece, Position finPos) {
        return Optional.empty();
    }

    @Override
    public Optional<Piece> pieceCapturedByMove(Chess game, Position initPos, Position finPos) {
        return Optional.empty();
    }

    @Override
    public Optional<CastlingType> castlingTypeOfPlay(Chess game, Position initPos, Position finPos) {
        return Optional.empty();
    }

    @Override
    public Optional<CastlingType> castlingTypeOfPlay(Chess game, Piece piece, Position finPos) {
        return Optional.empty();
    }

    @Override
    public boolean isPlayerInCheck(Chess game, ChessColor color) {
        return false;
    }

    @Override
    public boolean doesThisMovementCauseACheck(Chess game, Piece piece, Position finPos) {
        return false;
    }

    @Override
    public boolean doesThisMovementCauseACheck(Chess game, Position initPos, Position finPos) {
        return false;
    }

    @Override
    public OptionalInt getEnPassantXDir(Chess game, Piece piece) {
        return OptionalInt.empty();
    }

    @Override
    public boolean isValidMove(Chess game, Position initPos, Position finPos) {
        return false;
    }

}
