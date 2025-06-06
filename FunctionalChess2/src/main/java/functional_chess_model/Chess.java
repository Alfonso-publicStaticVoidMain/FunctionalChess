package functional_chess_model;

import functional_chess_model.Pieces.King;
import functional_chess_model.Pieces.Pawn;
import functional_chess_model.Pieces.Rook;
import functional_chess_model.rules_engine.RulesEngine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 *
 * @param pieces {@link List} of {@link Piece}s involved in the game.
 * @param castling {@link Map} that maps each {@link ChessColor} to another
 * map mapping each {@link CastlingType} to a boolean representing the
 * availability of castling for that player and castling variant.
 * @param playHistory {@link List} of {@link Play}s previously done in the game.
 * @param variant {@link GameVariant} object storing certain qualities of
 * the game like number of rows, columns, initial rows and columns for each
 * color and certain types of initPieces and castling columns for each variant of
 * castling.
 * @param state {@link GameState} enum representing if the game has started,
 * ended, and how it ended, or if it's currently in progress.
 * @author Alfonso Gallego
 */
public record Chess(
    List<Piece> pieces,
    Map<ChessColor, Map<CastlingType, Boolean>> castling,
    List<Play> playHistory,
    ChessColor activePlayer,
    GameVariant variant,
    GameState state,
    boolean isTimed,
    int whiteSeconds,
    int blackSeconds
) implements Serializable {

    //TODO: Introduce a RulesEngine parameter to the update state functions.

    //<editor-fold defaultstate="collapsed" desc="Update state functions">

    /**
     * Attempts to perform a movement and returns the state of the game after it
     * has been performed, or {@code Optional.empty} if it was illegal.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move the piece to.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal.
     */
    public Optional<Chess> tryToMove(Piece piece, Position finPos, boolean checkCheck, RulesEngine rules) {
        ChessColor playerMoving = piece.getColor();
        Position initPos = piece.getPosition();
        if (!rules.isValidMove(this, piece, finPos, checkCheck)) return Optional.empty();

        // Store the piece after being moved and piece captured if present.
        Piece pieceAfterMoving = piece.moveTo(finPos);
        Piece pieceCaptured = rules.pieceCapturedByMove(this, piece, finPos).orElse(null);
        return Optional.of(
            Chess.Builder.of(this)
                .withPieces(updatedPiecesAfterMove(piece, pieceAfterMoving, pieceCaptured))
                .withCastling(updatedCastlingAfterMove(playerMoving, initPos, pieceCaptured))
                .withPlayHistory(updatedPlaysAfterMove(initPos, finPos, pieceAfterMoving, pieceCaptured))
                .withOppositeActivePlayer()
                .withState(GameState.IN_PROGRESS)
                .build()
        );
    }

    /**
     * Attempts to perform a movement and returns the state of the game after it
     * has been performed, or {@code Optional.empty} if it was illegal.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal, or if the initial
     * position contained no piece.
     * @see Chess#findPieceAt
     */
    public Optional<Chess> tryToMove(Position initPos, Position finPos, boolean checkCheck, RulesEngine rules) {
        return findPieceThenApply(initPos, piece -> tryToMove(piece, finPos, checkCheck, rules));
    }

    /**
     * Overloaded version of {@link Chess#tryToMove(Position, Position, boolean, RulesEngine)},
     * defaulting checkCheck to true.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal, or if the initial
     * position contained no piece. Declares movements that would cause a check
     * as illegal.
     */
    public Optional<Chess> tryToMove(Position initPos, Position finPos, RulesEngine rules) {
        return tryToMove(initPos, finPos, true, rules);
    }

    /**
     * Overloaded version of {@link Chess#tryToMove(Piece, Position, boolean, RulesEngine)},
     * defaulting {@code checkCheck} to true.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move the piece to.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal. Declares movements
     * that would cause a check as illegal.
     */
    public Optional<Chess> tryToMove(Piece piece, Position finPos, RulesEngine rules) {
        return tryToMove(piece.getPosition(), finPos, true, rules);
    }

    /**
     * Attempts to perform a movement stored within a Play and returns the state
     * of the game after it has been performed, or {@code Optional.empty} if it
     * was illegal.
     * @param play {@link Play} containing the movement.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal.
     */
    public Optional<Chess> tryToMove(Play play, boolean checkCheck, RulesEngine rules) {
        return tryToMove(play.initPos(), play.finPos(), checkCheck, rules);
    }

    /**
     * Overloaded version of {@link Chess#tryToMove(Play, boolean, RulesEngine)}, defaulting
     * {@code checkCheck} to true.
     * @param play {@link Play} containing the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal. Declares movements
     * that would cause a check as illegal.
     */
    public Optional<Chess> tryToMove(Play play, RulesEngine rules) {
        return tryToMove(play.initPos(), play.finPos(), true, rules);
    }

    /**
     * Attempts to perform the variant of castling to the given player, if able,
     * and returns the state of the game after it's been done, or
     * {@code Optional.empty} if it was illegal.
     * @param player {@link ChessColor} of the player doing the castling.
     * @param castlingType {@link CastlingType} variant of the castling being done.
     * @return An {@code Optional} object containing the state of the game
     * after the castling has been performed, or {@code Optional.empty} if it
     * was illegal. Updates the values of {@code initPieces}, {@code playHistory},
     * {@code activePlayer} and {@code castling} for the returned game
     * accordingly.
     */
    public Optional<Chess> tryToCastle(ChessColor player, CastlingType castlingType, RulesEngine rules) {
        if (!isCastlingAvailable(player, castlingType)) return Optional.empty();
        Optional<Piece> kingOrNot = findPieceAt(rules.variant().initKingPos(player));
        Optional<Piece> rookOrNot = findPieceAt(rules.variant().initRookPos(castlingType, player));
        if (kingOrNot.isEmpty() || rookOrNot.isEmpty()) return Optional.empty();
        Piece king = kingOrNot.get();
        Piece rook = rookOrNot.get();
        return Optional.of(
            Chess.Builder.of(this)
                .withPieces(updatedPiecesAfterCastling(player, castlingType, king, rook))
                .withCastling(updatedCastlingAfterCastling(player))
                .withPlayHistory(updatedPlaysAfterCastling(player, castlingType, king))
                .withOppositeActivePlayer()
                .withState(GameState.IN_PROGRESS)
                .build()
        );
    }

    /**
     * Checks if the given player is in checkmate or if the game is a draw.
     * @param color {@link ChessColor} of the player being checked.
     * @return An {@code Optional} object containing the a {@code Chess} game
     * whose {@code state} attribute has been updated according to checkmate
     * and draw rules.
     * <br><br>
     * If the player is currently in check and with every possible move it could
     * make it'd still be in check, the state updates to a win of the opposite
     * player. If the player isn't in check but every possible move would put it
     * in check, the state is updated to DRAW.
     * <br><br>
     * If there's a move that player can make that doesn't end up in them being
     * in check, {@code Optional.empty} is returned.
     */
    public Optional<Chess> checkMate(ChessColor color, RulesEngine rules) {
        boolean isInCheck = rules.isPlayerInCheck(this, color);
        for (Piece piece : pieces.stream()
            .filter(p -> p.getColor() == color)
            .toList()
        ) {
            for (int col = 1; col <= variant.cols(); col++) {
                for (int row = 1; row <= variant.rows(); row++) {
                    Position pos = Position.of(col, row);
                    if (piece.canMove(this, pos)) {
                        Chess gameAfterMovement = tryToMoveChain(piece.getPosition(), pos, rules);
                        if (!rules.isPlayerInCheck(gameAfterMovement, color)) return Optional.empty();
                    }
                }
            }
        }
        return Optional.of(
            Chess.Builder.of(this)
                .withState(isInCheck ? GameState.playerWins(color.opposite()) : GameState.DRAW)
                .build()
        );
    }

    /**
     * Crowns a {@link Pawn} piece passed as argument to a new variant, and returns
     * the game state after than change has been done.
     * @param piece {@link Piece} to crown.
     * @param newType String representing the new variant to crown it to.
     * @return An optional containing the state of the game after crowning the
     * argument piece to its new variant, or {@code Optional.empty} if the piece
     * isn't a Pawn, if it isn't the piece moved on the last {@link Play} of the
     * game, or if it's not located on its crowning row.
     */
    public Optional<Chess> crownPawn(Piece piece, String newType) {
        if (!(piece instanceof Pawn) || playHistory.isEmpty()) return Optional.empty();
        Play lastPlay = getLastPlay().get();
        if (!piece.equals(lastPlay.piece())) return Optional.empty();
        Position pos = piece.getPosition();
        ChessColor color = piece.getColor();
        if (pos.y() != variant.crowningRow(color)) return Optional.empty();
        Piece crownedPiece = PieceType.valueOf(newType.toUpperCase()).constructor(pos, color);

        return Optional.of(
            Chess.Builder.of(this)
                .withPieces(updatedPiecesAfterCrowning(piece, crownedPiece))
                .withPlayHistory(updatedPlaysAfterCrowning(piece, lastPlay, crownedPiece))
                .withState(GameState.IN_PROGRESS)
                .build()
        );
    }

    /**
     * Updates the number of seconds left for the white and black player
     * according to the received arguments.
     * @param whiteSeconds Number of seconds for the white player to update to.
     * @param blackSeconds Number of seconds for the black player to update to.
     * @return A new Chess game with all attributes copies from {@code this} except
     * {@code whiteSeconds} and {@code blackSeconds}, which are taken from this
     * method's parameters.
     */
    public Chess withSeconds(int whiteSeconds, int blackSeconds) {
        return Chess.Builder.of(this)
            .withSeconds(whiteSeconds, blackSeconds)
            .build();
        //return new Chess(pieces, castling, playHistory, activePlayer, variant, state, isTimed, whiteSeconds, blackSeconds);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Update Pieces, Castling and Plays Functions">

    /**
     * Updates the list of pieces according to a move performed.
     * @param pieceBeforeMoving {@link Piece} in its position before moving.
     * @param pieceAfterMoving {@link Piece} in its position after moving.
     * @param pieceCaptured {@link Piece} captured, or {@code null} if none was.
     * @return A list of pieces reflecting the movement that was done:
     * <ul>
     *     <li>{@code pieceBeforeMoving} is removed.</li>
     *     <li>{@code pieceCaptured} is removed if not null.</li>
     *     <li>{@code pieceAfterMoving} is added.</li>
     * </ul>
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Piece> updatedPiecesAfterMove(Piece pieceBeforeMoving, Piece pieceAfterMoving, Piece pieceCaptured) {
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        if (pieceCaptured != null) updatedPieces.remove(pieceCaptured);
        updatedPieces.remove(pieceBeforeMoving);
        updatedPieces.add(pieceAfterMoving);
        return List.copyOf(updatedPieces);
    }

    /**
     * Updates the list of plays according to a movement performed.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @param pieceMoved {@link Piece} that was moved.
     * @param pieceCaptured {@link Piece} captured, or {@code null} if none was.
     * @return A list of plays reflecting the movement that was done: A new
     * {@link Play} is added with parameters {@code pieceMoved}, {@code initPos},
     * {@code finPos} and {@code pieceCaptured}.
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Play> updatedPlaysAfterMove(Position initPos, Position finPos, Piece pieceMoved, Piece pieceCaptured) {
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.add(new Play(pieceMoved, initPos, finPos, pieceCaptured));
        return List.copyOf(updatedPlays);
    }

    /**
     * Updates the castling map according to a movement performed.
     * @param playerMoving Player who performed the movement.
     * @param initPos Initial {@link Position} of the movement.
     * @param pieceCaptured {@link Piece} captured, or {@code null} if none was.
     * @return A castling map reflecting the movement that was done:
     * <ul>
     *     <li>If the active player moved from the initial position of a Rook or King, the appropriate castling
     *     availabilities are set to false for that player.</li>
     *     <li>If the nonactive player had one of its Rooks captured in the movement, the appropriate castling
     *     availability is set to false for that player.</li>
     * </ul>
     * The returned map is first passed to Map.copyOf to ensure immutability.
     */
    private Map<ChessColor, Map<CastlingType, Boolean>> updatedCastlingAfterMove(ChessColor playerMoving, Position initPos, Piece pieceCaptured) {
        Map<ChessColor, Map<CastlingType, Boolean>> updatedCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> updatedCastlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                if (
                    color.equals(playerMoving) // The color we're checking is the color of the player that just moved
                        && isCastlingAvailable(color, type) // Castling was available before the movement for this color and variant
                        && ( // The initial position of the movement was the king or rook's initial position of the castling variant we're checking
                        initPos.equals(variant.initRookPos(type, color))
                            || initPos.equals(variant.initKingPos(color))
                    )) updatedCastlingForColor.put(type, false);
                else if (
                    pieceCaptured != null // A piece was captured
                        && isCastlingAvailable(color, type) // Castling was available before the movement for this color and variant
                        && color.equals(pieceCaptured.getColor()) // The color we're checking is the same as the captured piece
                        && pieceCaptured.getPosition().equals(variant.initRookPos(type, color)) // The captured piece was in the rook initial position of the castling variant we're checking
                ) updatedCastlingForColor.put(type, false);
                else updatedCastlingForColor.put(type, isCastlingAvailable(color, type));
            }
            updatedCastling.put(color, Map.copyOf(updatedCastlingForColor));
        }
        return Map.copyOf(updatedCastling);
    }

    /**
     * Updates the list of pieces according to a castling performed.
     * @param player Player who performed the castling.
     * @param castlingType Type of castling performed.
     * @param king {@link King} {@link Piece} moved.
     * @param rook {@link Rook} {@link Piece} moved.
     * @return A list of pieces reflecting the castling that was done:
     * <ul>
     *     <li>The {@link King} and {@link Rook} are removed.</li>
     *     <li>A new {@link King} and {@link Rook} are instantiated and added with the position
     *     appropriate for the castling that was performed.</li>
     * </ul>
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Piece> updatedPiecesAfterCastling(ChessColor player, CastlingType castlingType, Piece king, Piece rook) {
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        updatedPieces.remove(king);
        updatedPieces.remove(rook);
        updatedPieces.add(king.moveTo(variant.castlingKingPos(castlingType, player)));
        updatedPieces.add(rook.moveTo(variant.castlingRookPos(castlingType, player)));
        return List.copyOf(updatedPieces);
    }

    /**
     * Updates the list of plays according to a castling performed.
     * @param player Player who performed the castling.
     * @param castlingType Type of castling performed.
     * @param king {@link King} {@link Piece} moved.
     * @return A list of plays reflecting the castling done: A new
     * {@link Play} is added with parameters {@code king}, the King's
     * initial position, its final position after performing the
     * castling and {@code castlingType}.
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Play> updatedPlaysAfterCastling(ChessColor player, CastlingType castlingType, Piece king) {
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.add(new Play(king, variant.initKingPos(player), variant.castlingKingPos(castlingType, player), castlingType));
        return List.copyOf(updatedPlays);
    }

    /**
     * Updates the castling map according to a castling performed.
     * @param player Player who performed the castling.
     * @return A castling map where the {@code player} has both types of castling availabilities
     * set to false.
     * The returned map is first passed to Map.copyOf to ensure immutability.
     */
    private Map<ChessColor, Map<CastlingType, Boolean>> updatedCastlingAfterCastling(ChessColor player) {
        Map<ChessColor, Map<CastlingType, Boolean>> updatedCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> updatedCastlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                if (color == player) updatedCastlingForColor.put(type, false);
                else updatedCastlingForColor.put(type, isCastlingAvailable(color, type));
            }
            updatedCastling.put(color, Map.copyOf(updatedCastlingForColor));
        }
        return Map.copyOf(updatedCastling);
    }

    /**
     * Updates the list of pieces according to a crowning performed.
     * @param piece {@link Piece} (will always be a {@link Pawn}) before crowning.
     * @param crownedPiece {@link Piece} after crowning.
     * @return A list of pieces reflecting the crowning done: The piece before crowning is removed and
     * the piece after crowning is added.
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Piece> updatedPiecesAfterCrowning(Piece piece, Piece crownedPiece) {
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        updatedPieces.remove(piece);
        updatedPieces.add(crownedPiece);
        return List.copyOf(updatedPieces);
    }

    /**
     * Updates the list of plays according to a crowning performed.
     * @param piece {@link Piece} (will always be a {@link Pawn}) before crowning.
     * @param lastPlay Last {@link Play} of the game.
     * @param crownedPiece {@link Piece} after crowning.
     * @return A list of plays reflecting the crowning done: The last play is removed
     * and replaced with another that includes what {@link Piece} the pawn was crowned
     * into.
     * The returned list is first passed to List.copyOf to ensure immutability.
     */
    private List<Play> updatedPlaysAfterCrowning(Piece piece, Play lastPlay, Piece crownedPiece) {
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.remove(playHistory.size() - 1);
        updatedPlays.add(new Play(piece, lastPlay.initPos(), lastPlay.finPos(), lastPlay.pieceCaptured(), crownedPiece));
        return List.copyOf(updatedPlays);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Monad Functions">

    /**
     * flatMap function of this Monad that applies a function to {@code this}
     * and returns the transformed object, or {@code this} if the function
     * returned {@code Optional.empty}.
     * @param f function to apply to {@code this}.
     * @return The state of the game after the transformation by the function
     * {@code f}, or {@code this} if that function returned
     * {@code Optional.empty}.
     */
    public Chess flatMap(Function<Chess, Optional<Chess>> f) {
        return f.apply(this).orElse(this);
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Position, Position, boolean, RulesEngine)}.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Position initPos, Position finPos, boolean checkCheck, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos, checkCheck, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Position, Position, RulesEngine)}.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Position initPos, Position finPos, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Piece, Position, boolean, RulesEngine)}.
     * @param piece {@link Piece} being moved.
     * @param finPos Final {@link Position} of the movement.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Piece piece, Position finPos, boolean checkCheck, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(piece, finPos, checkCheck, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Piece, Position, RulesEngine)}.
     * @param piece {@link Piece} being moved.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Piece piece, Position finPos, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(piece, finPos, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Play, boolean, RulesEngine)}.
     * @param play {@link Play} storing the movement.
     * @param checkCheck State parameter to track whether we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Play play, boolean checkCheck, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(play, checkCheck, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToMove(Play, boolean, RulesEngine)}.
     * @param play {@link Play} storing the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Play play, RulesEngine rules) {
        return flatMap(chess -> chess.tryToMove(play, rules));
    }

    /**
     * Monadic version of {@link Chess#checkMate(ChessColor, RulesEngine)}.
     * @param color {@link ChessColor} of the player being checked.
     * @return The state of the game after checking if the parameter player is
     * in checkmate or if the game is a draw, or {@code this} if that player
     * isn't in check and the game isn't a draw.
     */
    public Chess checkMateChain(ChessColor color, RulesEngine rules) {
        return flatMap(chess -> chess.checkMate(color, rules));
    }

    /**
     * Monadic version of {@link Chess#tryToCastle(ChessColor, CastlingType, RulesEngine)}
     * @param player {@link ChessColor} of the player doing the castling.
     * @param castlingType {@link CastlingType} variant of the castling being done.
     * @return The state of the game after the castling has been performed, or
     * {@code this} if it was illegal.
     */
    public Chess tryToCastleChain(ChessColor player, CastlingType castlingType, RulesEngine rules) {
        return flatMap(chess -> chess.tryToCastle(player, castlingType, rules));
    }

    /**
     * Monadic version of {@link Chess#crownPawn(Piece, String)}.
     * @param piece {@link Piece} to crown.
     * @param newType String representing the new variant to crown it to.
     * @return The state of the game after crowning the argument piece to its
     * new variant, or {@code this} if the piece isn't a Pawn, if it isn't the
     * piece moved on the last {@link Play} of the game, or if it's not located
     * on its crowning row.
     */
    public Chess crownPawnChain(Piece piece, String newType) {
        return flatMap(chess -> chess.crownPawn(piece, newType));
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Various auxiliary methods">

    /**
     * Gets the last play done, if able.
     * @return An {@code Optional} containing the last {@link Play} stored in the
     * {@code playHistory} attribute, if present, or {@code Optional.empty} otherwise.
     */
    public Optional<Play> getLastPlay() {
        if (playHistory.isEmpty()) return Optional.empty();
        return Optional.of(playHistory.getLast());
    }

    /**
     * Gets the piece present at the given position, if able.
     * @param pos {@link Position} to find a {@link Piece} in.
     * @return The {@link Piece} found in the parameter position if there's one,
     * otherwise returns {@code Optional.empty}.
     */
    public Optional<Piece> findPieceAt(Position pos) {
        return pieces.stream()
            .filter(piece -> piece.getPosition().equals(pos))
            .findAny();
    }

    /**
     * Checks whether there's a piece or not in the given position.
     * @param pos {@link Position} to check.
     * @return True if there's a {@link Piece} in the parameter {@link Position}, false otherwise.
     */
    public boolean checkPieceAt(Position pos) {
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos));
    }

    /**
     * Checks whether there's a piece of the same color in the given position.
     * @param pos {@link Position} to check.
     * @param color {@link ChessColor} to compare to.
     * @return True if there's a {@link Piece} in the parameter position, and
     * its color is the parameter color, false otherwise.
     */
    public boolean checkPieceSameColorAs(Position pos, ChessColor color) {
        return findPieceThenTest(pos, piece -> piece.getColor() == color);
    }

    /**
     * Checks whether there's a piece of a different color in the given position.
     * @param pos {@link Position} to check.
     * @param color {@link ChessColor} to compare to.
     * @return True if there's a {@link Piece} in the parameter position, and
     * its color is not the parameter color, false otherwise.
     */
    public boolean checkPieceDiffColorAs(Position pos, ChessColor color) {
        return findPieceThenTest(pos, piece -> piece.getColor() != color);
    }

    /**
     * Gets the royal piece of the given color.
     * @param color {@link ChessColor} to match.
     * @return The {@link Piece} of the parameter color whose {@code royal}
     * attribute is true, or {@code Optional.empty} if there's none. If somehow
     * there are multiple royal pieces, this method might return a different
     * one on each call.
     */
    public Optional<Piece> findRoyalPiece(ChessColor color) {
        return pieces.stream()
            .filter(piece -> piece.isRoyal() && piece.getColor() == color)
            .findAny();
    }

    /**
     * Gets the availability of castling for the given player and variant.
     * @param color {@link ChessColor} player to check.
     * @param type {@link CastlingType} variant to check.
     * @return Gets the {@code variant} key from the {@code color} key of the
     * castling map of {@code this}.
     */
    public boolean isCastlingAvailable(ChessColor color, CastlingType type) {
        return castling.get(color).get(type);
    }

    public <T> Optional<T> findPieceThenApply(Position initPos, Function<Piece, Optional<T>> f) {
        return findPieceAt(initPos)
            .flatMap(f);
    }

    public <T> T findPieceThenApply(Position initPos, Function<Piece, T> f, T fallback) {
        return findPieceAt(initPos)
            .map(f)
            .orElse(fallback);
    }

    public boolean findPieceThenTest(Position initPos, Predicate<Piece> condition) {
        return findPieceThenApply(initPos, condition::test, false);
    }

    /**
     * Checks the collision along a path following a Rook or Bishop-like
     * movement, ie, in a straight line or a diagonal.
     * @param initPos Initial Position of the movement.
     * @param finPos Final Position of the movement.
     * @return Returns true if there's no {@link Piece} along the trajectory
     * of the movement from {@code initPos} to {@code finPos}, both exclusive.
     * The method will return false if the movement isn't on a straight line or
     * diagonal.
     */
    public boolean isPathClear(Position initPos, Position finPos) {
        return isPathClear(Movement.of(initPos, finPos));
    }

    public boolean isPathClear(Movement move) {
        if (!move.isDiagonal() && !move.isStraight()) return false;
        int dx = move.dx();
        int dy = move.dy();
        return IntStream.range(1, Math.max(Math.abs(dx), Math.abs(dy)))
            .mapToObj(n -> Position.of(move.initX() + n* Integer.compare(dx, 0), move.initY() + n* Integer.compare(dy, 0)))
            .noneMatch(this::checkPieceAt);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Builder Inner Class">

    public static class Builder {
        private List<Piece> pieces;
        private Map<ChessColor, Map<CastlingType, Boolean>> castling;
        private List<Play> playHistory;
        private ChessColor activePlayer;
        private GameVariant variant;
        private GameState state;
        private boolean isTimed;
        private int whiteSeconds;
        private int blackSeconds;

        private Builder() {}

        static Builder blank() {return new Builder();}

        static Builder of(Chess original) {
            Builder builder = new Builder();
            builder.pieces = original.pieces();
            builder.castling = original.castling();
            builder.playHistory = original.playHistory();
            builder.activePlayer = original.activePlayer();
            builder.variant = original.variant();
            builder.state = original.state();
            builder.isTimed = original.isTimed();
            builder.whiteSeconds = original.whiteSeconds();
            builder.blackSeconds = original.blackSeconds();
            return builder;
        }

        Builder withPieces(List<Piece> pieces) {
            this.pieces = pieces;
            return this;
        }

        Builder withCastling(Map<ChessColor, Map<CastlingType, Boolean>> castling) {
            this.castling = castling;
            return this;
        }

        Builder withPlayHistory(List<Play> playHistory) {
            this.playHistory = playHistory;
            return this;
        }

        Builder withActivePlayer(ChessColor activePlayer) {
            this.activePlayer = activePlayer;
            return this;
        }

        Builder withOppositeActivePlayer() {
            return withActivePlayer(activePlayer.opposite());
        }

        Builder withVariant(GameVariant variant) {
            this.variant = variant;
            return this;
        }

        Builder withState(GameState state) {
            this.state = state;
            return this;
        }

        Builder withIsTimed(boolean isTimed) {
            this.isTimed = isTimed;
            return this;
        }

        Builder withSeconds(int whiteSeconds, int blackSeconds) {
            this.whiteSeconds = whiteSeconds;
            this.blackSeconds = blackSeconds;
            return this;
        }

        Builder withSeconds(int seconds) {
            return withSeconds(seconds, seconds);
        }

        Chess build() {
            return new Chess(
                pieces,
                castling,
                playHistory,
                activePlayer,
                variant,
                state,
                isTimed,
                whiteSeconds,
                blackSeconds
            );
        }
    }

    //</editor-fold>
}