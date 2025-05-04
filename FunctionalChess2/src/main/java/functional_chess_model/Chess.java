package functional_chess_model;

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
 * @author Alfonso Gallego
 */
public record Chess(
    List<Piece> pieces,
    Map<ChessColor, Map<CastlingType, Boolean>> castling,
    List<Play> playHistory,
    ChessColor activePlayer,
    GameConfiguration config,
    GameState state
) {
    
    /*
    Notes to self:
        * I have to be very careful with the usage of the activePlayer attribute
    within Chess, specially when there are times that moves or checks are being
    made before the update of the activePlayer takes place.
    
    */
    
    
    //<editor-fold defaultstate="collapsed" desc="Chess -> Optional<Chess> functions">
    
    public Optional<Chess> tryToMove(Position initPos, Position finPos, boolean checkCheck) {
        Optional<Piece> pieceOrNot = findPieceAt(initPos);
        if (pieceOrNot.isEmpty()) return Optional.empty();
        Piece pieceToMove = pieceOrNot.get();
        ChessColor playerMoving = pieceToMove.getColor();
        Optional<Piece> pieceCaptured = pieceCapturedByMove(pieceToMove, finPos);
        Optional<CastlingType> castlingInfo = castlingTypeOfPlay(pieceToMove, finPos);
        
        if (!pieceToMove.isLegalMovement(this, finPos, checkCheck) && castlingInfo.isEmpty()) return Optional.empty();
        
        List<Piece> updatedPieces = new ArrayList<>(pieces);
        if (pieceCaptured.isPresent()) updatedPieces.remove(pieceCaptured.get());
        updatedPieces.remove(pieceToMove);
        Piece pieceMoved = pieceToMove.moveTo(finPos);
        updatedPieces.add(pieceMoved);
        
        castlingInfo.ifPresent(castlingInfoContained -> {
            Piece rookToCastleWith = findPieceAt(config.rookInitPos(playerMoving, castlingInfoContained)).get();
            updatedPieces.remove(rookToCastleWith);
            updatedPieces.add(rookToCastleWith.moveTo(config.rookCastlingPos(playerMoving, castlingInfoContained)));
        });
        
        List<Play> updatedPlays = new LinkedList<>(playHistory);
        updatedPlays.add(new Play(pieceMoved, initPos, finPos, pieceCaptured, castlingInfo));
        
        Map<ChessColor, Map<CastlingType, Boolean>> updatedCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> updatedCastlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                if (color.equals(playerMoving) && castling.get(color).get(type) && (
                        castlingInfo.isPresent() || initPos.equals(config.rookInitPos(color, type))
                )) updatedCastlingForColor.put(type, false);
                else updatedCastlingForColor.put(type, castling.get(color).get(type));
            }
            updatedCastling.put(color, updatedCastlingForColor);
        }
        return Optional.of(new Chess(List.copyOf(updatedPieces), Map.copyOf(updatedCastling), List.copyOf(updatedPlays), activePlayer.opposite(), config, GameState.IN_PROGRESS));
    }
    
    public Optional<Chess> tryToMove(Position initPos, Position finPos) {
        return tryToMove(initPos, finPos, true);
    }
    
    public Optional<Chess> tryToMove(Piece piece, Position finPos, boolean checkCheck) {
        return tryToMove(piece.getPosition(), finPos, checkCheck);
    }
    
    public Optional<Chess> tryToMove(Piece piece, Position finPos) {
        return tryToMove(piece.getPosition(), finPos, true);
    }
    
    public Optional<Chess> tryToMove(Play play, boolean checkCheck) {
        return tryToMove(play.initPos(), play.finPos(), checkCheck);
    }
    
    public Optional<Chess> tryToMove(Play play) {
        return tryToMove(play.initPos(), play.finPos(), true);
    }
    
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
        // No valid movement causes the player to not be in check anymore, so the game ends with a victory for the opposing player, or a draw if the "color" player wasn't in check.
        return Optional.of(new Chess(pieces, castling, playHistory, activePlayer, config, isInCheck ? GameState.playerWins(color.opposite()) : GameState.DRAW));
    }
    
    public Optional<Chess> crownPawn(Piece piece, String newType) {
        if (!(piece instanceof Pawn)) return Optional.empty();
        Position pos = piece.getPosition();
        ChessColor color = piece.getColor();
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
        return Optional.of(new Chess(updatedPieces, castling, playHistory, activePlayer, config, GameState.IN_PROGRESS));
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Monad Functions">
    
    public Chess flatMap(Function<Chess, Optional<Chess>> f) {
        return f.apply(this).orElse(this);
    }
    
    public Chess tryToMoveChain(Position initPos, Position finPos, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos, checkCheck));
    }
    
    public Chess tryToMoveChain(Position initPos, Position finPos) {
        return flatMap(chess -> chess.tryToMove(initPos, finPos));
    }
    
    public Chess tryToMoveChain(Piece piece, Position finPos, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(piece, finPos, checkCheck));
    }
    
    public Chess tryToMoveChain(Piece piece, Position finPos) {
        return flatMap(chess -> chess.tryToMove(piece, finPos));
    }
    
    public Chess tryToMoveChain(Play play, boolean checkCheck) {
        return flatMap(chess -> chess.tryToMove(play, checkCheck));
    }
    
    public Chess tryToMoveChain(Play play) {
        return flatMap(chess -> chess.tryToMove(play));
    }
    
    public Chess checkMateChain(ChessColor color) {
        return flatMap(chess -> chess.checkMate(color));
    }
    
    public Chess crownPawnChain(Piece piece, String newType) {
        return flatMap(chess -> chess.crownPawn(piece, newType));
    }
    
    //</editor-fold>
    
    public Optional<Play> getLastPlay() {
        if (playHistory.isEmpty()) return Optional.empty();
        return Optional.of(playHistory.get(playHistory.size() - 1));
    }
    
    public Optional<Piece> findPieceAt(Position pos) {
        return pieces.stream()
            .filter(piece -> piece.getPosition().equals(pos))
            .findFirst();
    }
    
    public boolean checkPieceAt(Position pos) {
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos));
    }
    
    public boolean checkPieceSameColorAs(Position pos, ChessColor color) {
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos) && piece.getColor() == color);
    }
    
    public boolean checkPieceDiffColorAs(Position pos, ChessColor color) {
        return pieces.stream()
            .anyMatch(piece -> piece.getPosition().equals(pos) && piece.getColor() != color);
    }
    
    public Optional<Piece> findRoyalPiece(ChessColor color) {
        return pieces.stream()
            .filter(piece -> piece.getColor() == color && piece.isRoyal())
            .findAny();
    }
    
    public Optional<Piece> pieceCapturedByMove(Piece piece, Position finPos) {
        if (checkPieceAt(finPos)) return findPieceAt(finPos);
        if (piece instanceof Pawn) {
            OptionalInt xDirEnPassantOrNot = getEnPassantXDir(piece);
            if (xDirEnPassantOrNot.isPresent() && xDirEnPassantOrNot.getAsInt() == -Position.xDist(piece.getPosition(), finPos)) return Optional.of(getLastPlay().get().piece());
        }
        return Optional.empty();
    }
    
    public Optional<Piece> pieceCapturedByMove(Position initPos, Position finPos) {
        Optional<Piece> pieceFound = findPieceAt(initPos);
        if (pieceFound.isEmpty()) return Optional.empty();
        return pieceCapturedByMove(pieceFound.get(), finPos);
    }
    
    public Optional<CastlingType> castlingTypeOfPlay(Piece piece, Position finPos) {
        if (!(piece instanceof King)) return Optional.empty();
        if (!finPos.equals(config.kingCastlingPos(piece.getColor(), CastlingType.LEFT)) && !finPos.equals(config.kingCastlingPos(piece.getColor(), CastlingType.RIGHT))) return Optional.empty();
        ChessColor color = piece.getColor();
        int initRow = config.initRow(color);
        if (isCastlingAvailable(color, CastlingType.LEFT) &&
            IntStream.rangeClosed(config.rookInitCol(CastlingType.LEFT)+1, config.rookCastlingCol(CastlingType.LEFT))
                .allMatch(x -> !checkPieceAt(Position.of(x, initRow))) &&
            IntStream.rangeClosed(config.kingCastlingCol(CastlingType.LEFT), config.kingInitCol())
                .noneMatch(x -> pieces.stream()
                    .anyMatch(p -> p.isLegalMovement(this, Position.of(x, initRow), false)))
        ) return Optional.of(CastlingType.LEFT);
        if (isCastlingAvailable(color, CastlingType.RIGHT) &&
            IntStream.rangeClosed(config.rookCastlingCol(CastlingType.RIGHT), config.rookInitCol(CastlingType.RIGHT)-1)
                .allMatch(x -> !checkPieceAt(Position.of(x, initRow))) &&
            IntStream.rangeClosed(config.kingInitCol(), config.kingCastlingCol(CastlingType.RIGHT))
                .noneMatch(x -> pieces.stream()
                    .anyMatch(p -> p.isLegalMovement(this, Position.of(x, initRow), false)))
        ) return Optional.of(CastlingType.RIGHT);
        return Optional.empty();
    }
    
    public boolean isCastlingAvailable(ChessColor color, CastlingType type) {
        return castling.get(color).get(type);
    }
    
    public boolean isPlayerInCheck(ChessColor color) {
        Piece royalPiece = findRoyalPiece(color).orElse(null);
        if (royalPiece == null) return false;
        Position royalPos = royalPiece.getPosition();
        return pieces.stream()
            .anyMatch(piece -> piece.getColor() != color && piece.isLegalMovement(this, royalPos, false));
    }
    
    public boolean doesThisMovementCauseACheck(Piece piece, Position finPos) {
        Optional<Chess> gameAfterMovementOrNot = tryToMove(piece.getPosition(), finPos, false);
        if (gameAfterMovementOrNot.isEmpty()) return false;
        return gameAfterMovementOrNot.get().isPlayerInCheck(piece.getColor());
    }
    
    public boolean wouldRoyalPiecesBeInConflict(Position initPos, Position finPos, ChessColor activePlayer) {
        Optional<Piece> royalPieceActivePlayerOrNot = findRoyalPiece(activePlayer);
        Optional<Piece> royalPieceOtherPlayerOrNot = findRoyalPiece(activePlayer.opposite());
        if (royalPieceActivePlayerOrNot.isEmpty() || royalPieceOtherPlayerOrNot.isEmpty()) return false;
        Piece royalPieceActivePlayer = royalPieceActivePlayerOrNot.get();
        Piece royalPieceOtherPlayer = royalPieceOtherPlayerOrNot.get();
        Optional<Chess> gameAfterMovementOrNot = tryToMove(initPos, finPos, false);
        if (gameAfterMovementOrNot.isEmpty()) return false;
        return royalPieceOtherPlayer.isLegalMovement(gameAfterMovementOrNot.get(), royalPieceActivePlayer.getPosition(), false);
    }
    
    public OptionalInt getEnPassantXDir(Piece piece) {
        Optional<Play> lastPlayOrNot = getLastPlay();
        
        if (lastPlayOrNot.isEmpty()) return OptionalInt.empty();
        
        Play lastPlay = lastPlayOrNot.get();
        Piece lastPieceMoved = lastPlay.piece();
        
        if (!(lastPieceMoved instanceof Pawn)) return OptionalInt.empty();
        if (lastPieceMoved.getColor() == piece.getColor()) return OptionalInt.empty();
        if (Math.abs(Position.yDist(lastPlay.initPos(), lastPlay.finPos())) != 2) return OptionalInt.empty();
        if (Math.abs(Position.xDist(lastPlay.finPos(), piece.getPosition())) != 1) return OptionalInt.empty();
        
        return OptionalInt.of(Position.xDist(lastPlay.finPos(), piece.getPosition()));
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
        
        int Xdirection = (Xmovement > 0) ? 1 : (Xmovement < 0) ? -1 : 0;
        int Ydirection = (Ymovement > 0) ? 1 : (Ymovement < 0) ? -1 : 0;
        int steps = Math.max(Math.abs(Xmovement), Math.abs(Ymovement));
        
        return IntStream.range(1, steps)
            .mapToObj(n -> Position.of(initX + n*Xdirection, initY + n*Ydirection))
            .noneMatch(position -> checkPieceAt(position));
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
    
    // Factory constructor methods:
    
    public static Map<ChessColor, Map<CastlingType, Boolean>> initialCastlingWith(boolean value) {
        Map<ChessColor, Map<CastlingType, Boolean>> result = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> castlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                castlingForColor.put(type, true);
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
    
    public static Chess almostChess() {
        GameConfiguration config = GameConfiguration.almostChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
    public static Chess capablancaChess() {
        GameConfiguration config = GameConfiguration.capablancaChess();
        return new Chess(config.pieces(), initialCastlingWith(true), List.of(), ChessColor.WHITE, config, GameState.NOT_STARTED);
    }
    
}
