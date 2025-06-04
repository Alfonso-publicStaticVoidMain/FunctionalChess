package functional_chess_model.rules_engine;

import functional_chess_model.*;

import java.util.Optional;
import java.util.OptionalInt;

public interface RulesEngine {

    Optional<Piece> pieceCapturedByMove(Chess game, Piece piece, Position finPos);

    Optional<Piece> pieceCapturedByMove(Chess game, Position initPos, Position finPos);

    Optional<CastlingType> castlingTypeOfPlay(Chess game, Position initPos, Position finPos);

    Optional<CastlingType> castlingTypeOfPlay(Chess game, Piece piece, Position finPos);

    boolean isPlayerInCheck(Chess game, ChessColor color);

    boolean doesThisMovementCauseACheck(Chess game, Piece piece, Position finPos);

    boolean doesThisMovementCauseACheck(Chess game, Position initPos, Position finPos);

    OptionalInt getEnPassantXDir(Chess game, Piece piece);

    boolean isValidMove(Chess game, Position initPos, Position finPos);

}
