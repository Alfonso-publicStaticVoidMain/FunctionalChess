package functional_chess_model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 *
 * @param pieces {@link List} of {@link Piece}s involved in the game.
 * @param castling {@link Map} that maps each {@link ChessColor} to another
 * map mapping each {@link CastlingType} to a boolean representing the
 * availability of castling for that player and castling type.
 * @param playHistory {@link List} of {@link Play}s previously done in the game.
 * @param config {@link GameConfiguration} object storing certain qualities of
 * the game like number of rows, columns, initial rows and columns for each
 * color and certain types of pieces and castling columns for each type of
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
    GameConfiguration config,
    GameState state
) implements Serializable {
    
    //<editor-fold defaultstate="collapsed" desc="Update state functions">
    
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
     * <br><br>
     * The following checks are performed, in this order:
     * <ol>
     * <li>If no piece is found in {@code initPos}, {@code Optional.empty} is
     * returned. Otherwise, that piece is stored in {@code piece}.</li>
     * <li>If {@code piece} can't legally move to {@code finPos} (with the
     * appropriate {@code checkCheck} parameter), {@code Optional.empty} is
     * returned.</li>
     * <li>Then a new pieces list is created and properly updated, taking into
     * account the possibility of a captured piece and an en passant capture.</li>
     * <li>Then a new Play list is created and updated.
     * <li>Then a new castling map is created and properly updated, setting to
     * false the possibility of castling for the active player if they moved
     * from the initial position of their king or rook on the appropiate side,
     * or for the nonactive player if the rook of the appropriate side was
     * captured in the movement.</li>
     * <li>Finally, a new {@code Chess} game is created and returned, copying
     * the lists and map created within the method to ensure immutability, and
     * passing them to the standard constructor of the class, while also
     * updating the active player to the opposite one, keeing the same game
     * configuration, and making the state IN_PROGRESS.</li>
     * </ol>
     * @see Chess#findPieceAt
     * @see Chess#pieceCapturedByMove
     */
    public Optional<Chess> tryToMove(Position initPos, Position finPos, boolean checkCheck) {
        Optional<Piece> pieceOrNot = findPieceAt(initPos);
        if (pieceOrNot.isEmpty()) return Optional.empty();
        return tryToMove(pieceOrNot.get(), finPos, checkCheck);
    }
    
    /**
     * Overloaded version of {@link Chess#tryToMove(Position, Position, boolean)},
     * defaulting checkCheck to true.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal, or if the initial
     * position contained no piece. Declares movements that would cause a check
     * as illegal.
     */
    public Optional<Chess> tryToMove(Position initPos, Position finPos) {
        return tryToMove(initPos, finPos, true);
    }
    
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
    public Optional<Chess> tryToMove(Piece piece, Position finPos, boolean checkCheck) {
        ChessColor playerMoving = piece.getColor();
        Position initPos = piece.getPosition();
        Optional<Piece> pieceCaptured = pieceCapturedByMove(piece, finPos);
        
        if (!piece.isLegalMovement(this, finPos, checkCheck)) return Optional.empty();
        
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        pieceCaptured.ifPresent(updatedPieces::remove);
        updatedPieces.remove(piece);
        Piece pieceMoved = piece.moveTo(finPos);
        updatedPieces.add(pieceMoved);
        
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.add(new Play(pieceMoved, initPos, finPos, pieceCaptured.orElse(null)));
        
        Map<ChessColor, Map<CastlingType, Boolean>> updatedCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> updatedCastlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                
                if (
                    color.equals(playerMoving) // The color we're checking is the same as the piece that moved
                    && isCastlingAvailable(color, type) // Casling was available before the movement for this color and type
                    && ( // The initial position of the movement was the king or rook's initial position of the castling type we're checking
                        initPos.equals(config.rookInitPos(color, type))
                        || initPos.equals(config.kingInitPos(color))
                    )
                ) updatedCastlingForColor.put(type, false);
                
                else if (
                    pieceCaptured.isPresent() // A piece was captured
                    && isCastlingAvailable(color, type) // Castling was available before the movement for this color and type
                    && color.equals(pieceCaptured.get().getColor()) // The color we're checking is the same as the captured piece
                    && pieceCaptured.get().getPosition().equals(config.rookInitPos(color, type)) // The captured piece was in the rook initial position of the castling type we're checking
                ) updatedCastlingForColor.put(type, false);
                
                else updatedCastlingForColor.put(type, isCastlingAvailable(color, type));
                
            }
            updatedCastling.put(color, Map.copyOf(updatedCastlingForColor));
        }
        return Optional.of(new Chess(List.copyOf(updatedPieces), Map.copyOf(updatedCastling), List.copyOf(updatedPlays), activePlayer.opposite(), config, GameState.IN_PROGRESS));
    }
    
    /**
     * Overloaded version of {@link Chess#tryToMove(Piece, Position, boolean)},
     * defaulting {@code checkCheck} to true.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move the piece to.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal. Declares movements
     * that would cause a check as illegal.
     */
    public Optional<Chess> tryToMove(Piece piece, Position finPos) {
        return tryToMove(piece.getPosition(), finPos, true);
    }
    
    /**
     * Attempts to perform a movement stored within a Play and returns the state
     * of the game after it has been performed, or {@code Optional.empty} if it
     * was illegal.
     * @param play {@link Play} containing the movement.
     * @param checkCheck State parameter to track whether or not we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal.
     */
    public Optional<Chess> tryToMove(Play play, boolean checkCheck) {
        return tryToMove(play.initPos(), play.finPos(), checkCheck);
    }
    
    /**
     * Overloaded version of {@link Chess#tryToMove(Play, boolean)}, defaulting
     * {@code checkCheck} to true.
     * @param play {@link Play} containing the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code Optional.empty} if that movement was illegal. Declares movements
     * that would cause a check as illegal.
     */
    public Optional<Chess> tryToMove(Play play) {
        return tryToMove(play.initPos(), play.finPos(), true);
    }
    
    /**
     * Attempts to perform the type of castling to the given player, if able,
     * and returns the state of the game after it's been done, or
     * {@code Optional.empty} if it was illegal.
     * @param player {@link ChessColor} of the player doing the castling.
     * @param castlingType {@link CastlingType} type of the castling being done.
     * @return An {@code Optional} object containing the state of the game
     * after the castling has been performed, or {@code Optional.empty} if it
     * was illegal. Updates the values of {@code pieces}, {@code playHistory},
     * {@code activePlayer} and {@code castling} for the returned game
     * accordingly.
     */
    public Optional<Chess> tryToCastle(ChessColor player, CastlingType castlingType) {
        Position kingInitPos = config.kingInitPos(player);
        Optional<Piece> kingOrNot = findPieceAt(kingInitPos);
        Optional<Piece> rookOrNot = findPieceAt(config.rookInitPos(player, castlingType));
        if (kingOrNot.isEmpty() || rookOrNot.isEmpty() || !isCastlingAvailable(player, castlingType)) return Optional.empty();
        Piece king = kingOrNot.get();
        Piece rook = rookOrNot.get();
        
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        updatedPieces.remove(king);
        updatedPieces.remove(rook);
        Position kingCastlingPos = config.kingCastlingPos(player, castlingType);
        Piece kingMoved = king.moveTo(kingCastlingPos);
        updatedPieces.add(kingMoved);
        updatedPieces.add(rook.moveTo(config.rookCastlingPos(player, castlingType)));
        
        Map<ChessColor, Map<CastlingType, Boolean>> updatedCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> updatedCastlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                if (color == player) updatedCastlingForColor.put(type, false);
                else  updatedCastlingForColor.put(type, isCastlingAvailable(color, type));
            }
            updatedCastling.put(color, Map.copyOf(updatedCastlingForColor));
        }
        
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.add(new Play(kingMoved, kingInitPos, kingCastlingPos, castlingType));
        
        return Optional.of(new Chess(List.copyOf(updatedPieces), Map.copyOf(updatedCastling), List.copyOf(updatedPlays), activePlayer.opposite(), config, GameState.IN_PROGRESS));
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
    public Optional<Chess> checkMate(ChessColor color) {
        boolean isInCheck = isPlayerInCheck(color);
        for (Piece piece : pieces.stream()
            .filter(p -> p.getColor() == color)
            .toList()
        ) {
            for (int col = 1; col < config.cols(); col++) {
                for (int row = 1; row < config.rows(); row++) {
                    Position pos = Position.of(col, row);
                    if (piece.isLegalMovement(this, pos, false)) {
                        Chess gameAfterMovement = tryToMoveChain(piece.getPosition(), pos);
                        if (!gameAfterMovement.isPlayerInCheck(color)) return Optional.empty();
                    }
                }
            }
        }
        return Optional.of(new Chess(pieces, castling, playHistory, activePlayer, config, isInCheck ? GameState.playerWins(color.opposite()) : GameState.DRAW));
    }
    
    /**
     * Crowns a {@link Pawn} piece passed as argument to a new type, and returns
     * the game state after than change has been done.
     * @param piece {@link Piece} to crown.
     * @param newType String representing the new type to crown it to.
     * @return An optional containing the state of the game after crowning the
     * argument piece to its new type, or {@code Optional.empty} if the piece
     * isn't a Pawn, if it isn't the piece moved on the last {@link Play} of the
     * game, or if it's not located on its crowning row.
     */
    public Optional<Chess> crownPawn(Piece piece, String newType) {
        if (!(piece instanceof Pawn) || playHistory.isEmpty()) return Optional.empty();
        Play lastPlay = getLastPlay().get();
        if (!piece.equals(lastPlay.piece())) return Optional.empty();
        Position pos = piece.getPosition();
        ChessColor color = piece.getColor();
        if (pos.y() != config.crowningRow(color)) return Optional.empty();
        
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        updatedPieces.remove(piece);
        switch (newType) {
            case "Queen" -> updatedPieces.add(new Queen(pos, color));
            case "Knight" -> updatedPieces.add(new Knight(pos, color));
            case "Rook" -> updatedPieces.add(new Rook(pos, color));
            case "Bishop" -> updatedPieces.add(new Bishop(pos, color));
            case "Amazon" -> updatedPieces.add(new Amazon(pos, color));
            case "Chancellor" -> updatedPieces.add(new Chancellor(pos, color));
            case "ArchBishop" -> updatedPieces.add(new ArchBishop(pos, color));
            default -> {return Optional.empty();}
        }
        
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        
        updatedPlays.remove(playHistory.size() - 1);
        updatedPlays.add(new Play(piece, lastPlay.initPos(), lastPlay.finPos(), lastPlay.pieceCaptured(), updatedPieces.getLast()));
        
        return Optional.of(new Chess(List.copyOf(updatedPieces), castling, List.copyOf(updatedPlays), activePlayer, config, GameState.IN_PROGRESS));
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
     * Monadic version of {@link Chess#tryToMove(Position, Position, boolean)}.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @param checkCheck State parameter to track whether or not we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Position initPos, Position finPos, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos, checkCheck));
    }
    
    /**
     * Monadic version of {@link Chess#tryToMove(Position, Position)}.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Position initPos, Position finPos) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos));
    }
    
    /**
     * Monadic version of {@link Chess#tryToMove(Piece, Position, boolean)}.
     * @param piece {@link Piece} being moved.
     * @param finPos Final {@link Position} of the movement.
     * @param checkCheck State parameter to track whether or not we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Piece piece, Position finPos, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(piece, finPos, checkCheck));
    }
    
    /**
     * Monadic version of {@link Chess#tryToMove(Piece, Position)}.
     * @param piece {@link Piece} being moved.
     * @param finPos Final {@link Position} of the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Piece piece, Position finPos) {
        return flatMap(chess -> chess.tryToMove(piece, finPos));
    }
    
    /**
     * Monadic version of {@link Chess#tryToMove(Play, boolean)}.
     * @param play {@link Play} storing the movement.
     * @param checkCheck State parameter to track whether or not we declare a
     * movement illegal if it'd cause a check.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */    
    public Chess tryToMoveChain(Play play, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(play, checkCheck));
    }
    
    /**
     * Monadic version of {@link Chess#tryToMove(Play, boolean)}.
     * @param play {@link Play} storing the movement.
     * @return The state of the game after the movement has been performed, or
     * {@code this} if the movement was illegal.
     */
    public Chess tryToMoveChain(Play play) {
        return flatMap(chess -> chess.tryToMove(play));
    }
    
    /**
     * Monadic version of {@link Chess#checkMate(ChessColor)}. 
     * @param color {@link ChessColor} of the player being checked.
     * @return The state of the game after checking if the parameter player is
     * in checkmate or if the game is a draw, or {@code this} if that player
     * isn't in check and the game isn't a draw.
     */
    public Chess checkMateChain(ChessColor color) {
        return flatMap(chess -> chess.checkMate(color));
    }
    
    /**
     * Monadic version of {@link Chess#tryToCastle(ChessColor, CastlingType)} 
     * @param player {@link ChessColor} of the player doing the castling.
     * @param castlingType {@link CastlingType} type of the castling being done.
     * @return The state of the game after the castling has been performed, or
     * {@code this} if it was illegal.
     */
    public Chess tryToCastleChain(ChessColor player, CastlingType castlingType) {
        return flatMap(chess -> chess.tryToCastle(player, castlingType));
    }
    
    /**
     * Monadic version of {@link Chess#crownPawn(Piece, String)}. 
     * @param piece {@link Piece} to crown.
     * @param newType String representing the new type to crown it to.
     * @return The state of the game after crowning the argument piece to its
     * new type, or {@code this} if the piece isn't a Pawn, if it isn't the
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
     * @return The last {@link Play} stored in the {@code playHistory} attribute,
     * if its present, or {@code Optional.empty} otherwise.
     */
    public Optional<Play> getLastPlay() {
        if (playHistory.isEmpty()) return Optional.empty();
        return Optional.of(playHistory.getLast());
    }
    
    /**
     * Gets the piece present at the given position, if able.
     * @param pos {@link Position} to find a {@link Piece} in.
     * @return The {@link Piece} found in the paratemer position if there's one,
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
     * @return True if there's a {@link Piece} in the parameter position, false
     * otherwise.
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
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos) && piece.getColor() == color);
    }
    
    /**
     * Checks whether there's a piece of a different color in the given position.
     * @param pos {@link Position} to check.
     * @param color {@link ChessColor} to compare to.
     * @return True if there's a {@link Piece} in the parameter position, and
     * its color is not the parameter color, false otherwise.
     */
    public boolean checkPieceDiffColorAs(Position pos, ChessColor color) {
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos) && piece.getColor() != color);
    }
    
    /**
     * Gets the royal piece of the given color.
     * @param color {@link ChessColor} to match.
     * @return The {@link Piece} of the paratemer color whose {@code royal}
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
     * Gets the piece that would be captured by the proposed move.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move the piece to.
     * @return The {@link Piece} that would be captured by the proposed move, or
     * {@code Optional.empty} it none would be. Ie, the piece present at
     * {@code finPos} for a regular capture, or in the appropriate position for
     * an en passant capture for {@link Pawn}s.
     */
    public Optional<Piece> pieceCapturedByMove(Piece piece, Position finPos) {
        if (checkPieceAt(finPos)) return findPieceAt(finPos);
        if (piece instanceof Pawn) {
            OptionalInt xDirEnPassantOrNot = getEnPassantXDir(piece);
            if (xDirEnPassantOrNot.isPresent() && xDirEnPassantOrNot.getAsInt() == Position.xDist(piece.getPosition(), finPos)) return Optional.of(getLastPlay().get().piece());
        }
        return Optional.empty();
    }
    
    /**
     * Gets the piece that would be captured by the proposed move.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return The {@link Piece} that would be captured by the proposed move, or
     * {@code Optional.empty} it none would be, or if there's no piece in the
     * initial position. Ie, the piece present at {@code finPos} for a regular
     * capture, or in the appropriate position for an en passant capture for
     * {@link Pawn}s.
     */
    public Optional<Piece> pieceCapturedByMove(Position initPos, Position finPos) {
        Optional<Piece> pieceFound = findPieceAt(initPos);
        if (pieceFound.isEmpty()) return Optional.empty();
        return pieceCapturedByMove(pieceFound.get(), finPos);
    }
    
    /**
     * Shows the kind of castling the movement is representing, accounting for
     * the legality of said castling.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return Optional.empty if the movement doesn't represent a castling
     * movement at all, and Optional[LEFT | RIGHT] if the movement represents a
     * castling, ie, if the moved piece is a King, the respective castling type
     * is still available for this game, the final position is the respective
     * castling position as dictated by this game's configuration, there's no
     * piece between the {@link King} and the {@link Rook} (though the piece in
     * that position might not be a Rook), and the King wouldn't be in check
     * during its movement.
     */
    public Optional<CastlingType> castlingTypeOfPlay(Position initPos, Position finPos) {
        Optional<Piece> pieceOrNot = findPieceAt(initPos);
        if (pieceOrNot.isEmpty()) return Optional.empty();
        Piece piece = pieceOrNot.get();
        return castlingTypeOfPlay(piece, finPos);
    }
    
    /**
     * Shows the kind of castling the movement is representing, accounting for
     * the legality of said castling.
     * @param piece Piece to move.
     * @param finPos Position to move the piece to.
     * @return Optional.empty if the movement doesn't represent a castling
     * movement at all, and Optional[LEFT | RIGHT] if the movement represents a
     * castling, ie, if the moved piece is a King, the respective castling type
     * is still available for this game, the final position is the respective
     * castling position as dictated by this game's configuration, there's no
     * piece between the {@link King} and the {@link Rook} (though the piece in
     * that position might not be a Rook), and the King wouldn't be in check
     * during its movement.
     */
    public Optional<CastlingType> castlingTypeOfPlay(Piece piece, Position finPos) {
        if (!(piece instanceof King)) return Optional.empty();
        if (!finPos.equals(config.kingCastlingPos(piece.getColor(), CastlingType.LEFT)) && !finPos.equals(config.kingCastlingPos(piece.getColor(), CastlingType.RIGHT))) return Optional.empty();
        ChessColor color = piece.getColor();
        int initRow = config.initRow(color);
        
        if (isCastlingAvailable(color, CastlingType.LEFT) && finPos.equals(config.kingCastlingPos(color, CastlingType.LEFT))) {
            // Checks if there are piece in the middle of the initial and castling positions
            if (IntStream.rangeClosed(config.rookInitCol(CastlingType.LEFT)+1, config.rookCastlingCol(CastlingType.LEFT))
                    .anyMatch(x -> checkPieceAt(Position.of(x, initRow))))
                return Optional.empty();
            
            // Checks if any piece could threaten to capture the King if it were on the middle positions.
            if (IntStream.rangeClosed(config.kingCastlingCol(CastlingType.LEFT), config.kingInitCol())
                    .anyMatch(x -> pieces.stream()
                            .anyMatch(p -> p.getColor() != color && p.isLegalMovement(this, Position.of(x, initRow), false))))
                return Optional.empty();
            
            return Optional.of(CastlingType.LEFT);
        }
        
        if (isCastlingAvailable(color, CastlingType.RIGHT) && finPos.equals(config.kingCastlingPos(color, CastlingType.RIGHT))) {
            // Checks if there are piece in the middle of the initial and castling positions
            if (IntStream.rangeClosed(config.rookCastlingCol(CastlingType.RIGHT), config.rookInitCol(CastlingType.RIGHT)-1)
                    .anyMatch(x -> checkPieceAt(Position.of(x, initRow))))
                return Optional.empty();
            
            // Checks if any piece could threaten to capture the King if it were on the middle positions.
            if (IntStream.rangeClosed(config.kingCastlingCol(CastlingType.RIGHT), config.kingInitCol())
                    .anyMatch(x -> pieces.stream()
                            .anyMatch(p -> p.getColor() != color && p.isLegalMovement(this, Position.of(x, initRow), false))))
                return Optional.empty();
            
            return Optional.of(CastlingType.RIGHT);
        }
        
        // If neither castling was available or the position wasn't the expected for that type, returns empty.
        return Optional.empty();
    }
    
    /**
     * Gets the availability of castling for the given player and type.
     * @param color {@link ChessColor} player to check.
     * @param type {@link CastlingType} type to check.
     * @return Gets the {@code type} key from the {@code color} key of the
     * castling map of {@code this}.
     */
    public boolean isCastlingAvailable(ChessColor color, CastlingType type) {
        return castling.get(color).get(type);
    }
    
    /**
     * Checks whether the given player is in check.
     * @param color {@link ChessColor} player to check check for.
     * @return True if the player is in check, ie, if there's a piece of the
     * opposite color that can legally capture their royal piece, ignoring if
     * making that move would cause a check for its controller. If the given
     * player has no royal pieces, returns false.
     */
    public boolean isPlayerInCheck(ChessColor color) {
        Optional<Piece> royalPieceOrNot = findRoyalPiece(color);
        return royalPieceOrNot.filter(royalPiece -> pieces.stream()
                .anyMatch(piece -> piece.getColor() != color && piece.isLegalMovement(this, royalPiece.getPosition(), false))).isPresent();
    }
    
    /**
     * Checks whether performing the given movement causes a check for the
     * player controlling the piece.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move it to.
     * @return True if, after performing the movement, the player is in check,
     * false otherwise.
     */
    public boolean doesThisMovementCauseACheck(Piece piece, Position finPos) {
        Optional<Chess> gameAfterMovementOrNot = tryToMove(piece.getPosition(), finPos, false);
        return gameAfterMovementOrNot.map(chess -> chess.isPlayerInCheck(piece.getColor())).orElse(false);
    }
    
    /**
     * Checks whether performing the given movement causes a check for the
     * player controlling the piece in the initial position.
     * @param initPos Initial {@link Position} of the movement.
     * @param finPos Final {@link Position} of the movement.
     * @return True if, after performing the movement, the player is in check,
     * false otherwise, or if there's no piece in the initial position.
     */
    public boolean doesThisMovementCauseACheck(Position initPos, Position finPos) {
        Optional<Piece> pieceOrNot = findPieceAt(initPos);
        return pieceOrNot.filter(piece -> doesThisMovementCauseACheck(piece, finPos)).isPresent();
    }
    
    /**
     * Gets the direction in the X axis the piece needs to move to capture
     * en passant the last piece moved, if that's a legal movement for it.
     * @param piece {@link Piece} to move.
     * @return An OptionalInt of +1 or -1 if the last {@link Play} was a
     * {@link Pawn} moving two cells from its starting position, {@code piece}
     * is also a {@link Pawn} of the opposite color and is within 1 unit from it
     * in the X axis.
     */
    public OptionalInt getEnPassantXDir(Piece piece) {
        Optional<Play> lastPlayOrNot = getLastPlay();
        
        if (lastPlayOrNot.isEmpty()) return OptionalInt.empty();
        
        Play lastPlay = lastPlayOrNot.get();
        Piece lastPieceMoved = lastPlay.piece();
        
        if (!(lastPieceMoved instanceof Pawn) || !(piece instanceof Pawn)) return OptionalInt.empty();
        if (lastPieceMoved.getColor() == piece.getColor()) return OptionalInt.empty();
        if (Math.abs(Position.yDist(lastPlay.initPos(), lastPlay.finPos())) != 2) return OptionalInt.empty();
        if (Math.abs(Position.xDist(lastPlay.finPos(), piece.getPosition())) != 1) return OptionalInt.empty();
        
        return OptionalInt.of(Position.xDist(piece.getPosition(), lastPlay.finPos()));
    }
    
    //</editor-fold>
        
    //<editor-fold defaultstate="collapsed" desc="Path checking methods">
    
    /**
     * Checks the collision along a path following a Rook or Bishop-like
     * movement, ie, in a straight line or a diagonal.
     * @param initPos Initial Position of the movement.
     * @param finPos Final Position of the movement.
     * @return Returns true if there's no {@link Piece} along the trajectory
     * of the movement from {@code initPos} to {@code finPos}, both exclusive.
     * The method will return false if the movement isn't on a straight line or
     * diagonal.
     * @see Chess#isPathClear(int, int, int, int)
     */
    public boolean isPathClear(Position initPos, Position finPos) {
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        return isPathClear(initPos.x(), initPos.y(), Xmovement, Ymovement);
    }
    
    /**
     * Checks the collision along a path following a Rook or Bishop-like
     * movement, ie, in a straight line or a diagonal.
     * @param initX Initial X coordinate of the movement.
     * @param initY Initial Y coordinate of the movement.
     * @param Xmovement Signed distance travelled in the X axis.
     * @param Ymovement Signed distance travelled in the Y axis.
     * @return Returns false is the path described isn't in a diagonal or
     * straight line, like a Bishop or Rook would make.
     * Then it returns true if there's no piece on each middle point of the
     * described path, excluding initial and final positions.
     * @see Chess#isPathClear(Position, Position)
     */
    public boolean isPathClear(int initX, int initY, int Xmovement, int Ymovement) {
        if (!isBishopLikePath(Xmovement, Ymovement) && !isRookLikePath(Xmovement, Ymovement)) return false;
        
        int Xdirection = Integer.compare(Xmovement, 0);
        int Ydirection = Integer.compare(Ymovement, 0);
        int steps = Math.max(Math.abs(Xmovement), Math.abs(Ymovement));
        
        return IntStream.range(1, steps)
            .mapToObj(n -> Position.of(initX + n*Xdirection, initY + n*Ydirection))
            .noneMatch(this::checkPieceAt);
    }
    
    /**
     * Checks if the proposed movement follows a straight path, like a Rook
     * would.
     * @param Xmovement Signed distance travelled in the X axis.
     * @param Ymovement Signed distance travelled in the Y axis.
     * @return False if both Xmovement and Ymovement are 0, true otherwise.
     */
    public static boolean isRookLikePath(int Xmovement, int Ymovement) {
        return !(Xmovement != 0 && Ymovement != 0);
    }
    
    /**
     * Overloaded version of {@link Chess#isRookLikePath(int, int)}, calculating
     * the X and Y movements from the coordinates of the given positions.
     * @param initPos Initial position of the movement.
     * @param finPos Final position of the movement.
     * @return False if both Xmovement and Ymovement are 0, true otherwise.
     */
    public static boolean isRookLikePath(Position initPos, Position finPos) {
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        return isRookLikePath(Xmovement, Ymovement);
    }
    
    /**
     * Checks if the proposed movement follows a diagonal path, like a Bishop
     * would.
     * @param Xmovement Signed distance travelled in the X axis.
     * @param Ymovement Signed distance travelled in the Y axis.
     * @return True if the absolute value of both X and Y movements is the same,
     * false otherwise.
     */
    public static boolean isBishopLikePath(int Xmovement, int Ymovement) {
        return Math.abs(Xmovement) == Math.abs(Ymovement);
    }
    
    /**
     * Overloaded version of {@link Chess#isBishopLikePath(int, int)},
     * calculating the X and Y movements from the coordinates of the given
     * positions.
     * @param initPos Initial position of the movement.
     * @param finPos Final position of the movement.
     * @return True if the absolute value of both X and Y movements is the same,
     * false otherwise.
     */
    public static boolean isBishopLikePath(Position initPos, Position finPos) {
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        return isBishopLikePath(Xmovement, Ymovement);
    }
    
    /**
     * Checks if the proposed movement matches the movement of a Knight.
     * @param Xmovement Signed distance travelled in the X axis.
     * @param Ymovement Signed distance travelled in the Y axis.
     * @return True if the absolute value of the sum of the X and Y movements
     * is exactly 3, and each of those absolute values is between 1 and 2, both
     * inclusive. False otherwise.
     */
    public static boolean isKnightLikePath(int Xmovement, int Ymovement) {
        return Math.abs(Xmovement) + Math.abs(Ymovement) == 3
            && Math.abs(Xmovement) <= 2 && Math.abs(Xmovement) >= 1
            && Math.abs(Ymovement) <= 2 && Math.abs(Ymovement) >= 1;
    }
    
    /**
     * Overloaded version of {@link Chess#isKnightLikePath(int, int)},
     * calculating the X and Y movements from the coordinates of the given
     * positions.
     * @param initPos Initial position of the movement.
     * @param finPos Final position of the movement.
     * @return True if the absolute value of the sum of the X and Y movements
     * is exactly 3, and each of those absolute values is between 1 and 2, both
     * inclusive. False otherwise.
     */
    public static boolean isKnightLikePath(Position initPos, Position finPos) {
        int Xmovement = Position.xDist(initPos, finPos);
        int Ymovement = Position.yDist(initPos, finPos);
        return isKnightLikePath(Xmovement, Ymovement);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Factory constructor methods">
    
    public static Map<ChessColor, Map<CastlingType, Boolean>> initialCastlingWith(boolean value) {
        Map<ChessColor, Map<CastlingType, Boolean>> result = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> castlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                castlingForColor.put(type, value);
            }
            result.put(color, castlingForColor);
        }
        return Map.copyOf(result);
    }
    
    /**
     * Factory method to create a standard game.
     * @return A game with all the standard game pieces on their respective
     * starting position.
     */
    public static Chess standardGame() {
        GameConfiguration config = GameConfiguration.standardGame();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess almostChessGame() {
        GameConfiguration config = GameConfiguration.almostChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess capablancaGame() {
        GameConfiguration config = GameConfiguration.capablancaChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess gothicGame() {
        GameConfiguration config = GameConfiguration.gothicChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess janusGame() {
        GameConfiguration config = GameConfiguration.janusChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess modernGame() {
        GameConfiguration config = GameConfiguration.modernChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess tuttiFruttiGame() {
        GameConfiguration config = GameConfiguration.tuttiFruttiChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    //</editor-fold>
    
}
