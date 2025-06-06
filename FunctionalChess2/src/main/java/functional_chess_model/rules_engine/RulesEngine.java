package functional_chess_model.rules_engine;

import functional_chess_model.*;

import java.util.Optional;
import java.util.OptionalInt;

public interface RulesEngine {

    GameVariant variant();

    Optional<Piece> pieceCapturedByMove(Chess game, Movement move);

    default Optional<Piece> pieceCapturedByMove(Chess game, Position initPos, Position finPos) {
        return pieceCapturedByMove(game, Movement.of(initPos, finPos));
    }

    Optional<CastlingType> castlingTypeOfPlay(Chess game, Movement move);

    default Optional<CastlingType> castlingTypeOfPlay(Chess game, Position initPos, Position finPos) {
        return castlingTypeOfPlay(game, Movement.of(initPos, finPos));
    }

    boolean isPlayerInCheck(Chess game, ChessColor color);

    boolean doesThisMovementCauseACheck(Chess game, Movement move);

    default boolean doesThisMovementCauseACheck(Chess game, Position initPos, Position finPos) {
        return doesThisMovementCauseACheck(game, Movement.of(initPos, finPos));
    }

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

    RulesEngine STANDARD_RULES = new StandardRules(GameVariant.STANDARD);
    RulesEngine ALMOST_CHESS_RULES = new StandardRules(GameVariant.ALMOSTCHESS);
    RulesEngine CAPABLANCA_RULES = new StandardRules(GameVariant.CAPABLANCA);
    RulesEngine GOTHIC_RULES = new StandardRules(GameVariant.GOTHIC);
    RulesEngine JANUS_RULES = new StandardRules(GameVariant.JANUS);
    RulesEngine MODERN_RULES = new StandardRules(GameVariant.MODERN);
    RulesEngine TUTTI_FRUTTI_RULES = new StandardRules(GameVariant.TUTTIFRUTTI);

}
