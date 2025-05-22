package functional_chess_model;

import functional_chess_model.Pieces.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Record storing certain information about the chess game being played.
 * @author Alfonso Gallego
 * @param initRow Map attribute mapping each {@link ChessColor} to the initial
 * row for regular pieces of that color.
 * @param initRowPawn Map attribute mapping each {@link ChessColor} to the
 * initial row for Pawns of that color.
 * @param crowningRow Map attribute mapping each {@link ChessColor} to the
 * row that Pawns of that color need to reach in order to crown.
 * @param rows Number of rows of the board.
 * @param cols Number of columns of the board.
 * @param pieces List of {@link Piece}s found in the initial configuration.
 * @param kingInitCol Initial column of the King.
 * @param rookInitCol Map mapping each castling type to the initial column of
 * the rook on that side. (The piece in that column might not be a Rook on
 * some configurations.)
 * @param kingCastlingCol Map mapping each castling type to the column of the
 * King when performing that type of castling.
 * @param rookCastlingCol Map mapping each castling type to the column of the
 * respective Rook when performing that type of castling.
 * @param crownablePieces Array of piece names that contain all pieces a Pawn
 * can crown into.
 * @param typeOfGame String representing the name of the chess variant being
 * played.
 */
public record GameConfiguration(
    boolean isTimed,
    int whiteSecondsMax,
    int blackSecondsMax,
    Map<ChessColor, Integer> initRow,
    Map<ChessColor, Integer> initRowPawn,
    Map<ChessColor, Integer> crowningRow,
    int rows,
    int cols,
    List<Piece> pieces,
    int kingInitCol,
    Map<CastlingType, Integer> rookInitCol,
    Map<CastlingType, Integer> kingCastlingCol,
    Map<CastlingType, Integer> rookCastlingCol,
    String[] crownablePieces,
    String typeOfGame
) implements Serializable {
    
    private static Map<ChessColor, Integer> initRowMap(int maxRows) {
        Map<ChessColor, Integer> initRowMap = new EnumMap<>(ChessColor.class);
        initRowMap.put(ChessColor.WHITE, 1);
        initRowMap.put(ChessColor.BLACK, maxRows);
        return Map.copyOf(initRowMap);
    }
    
    private static Map<ChessColor, Integer> initRowPawnMap(int maxRows) {
        Map<ChessColor, Integer> initRowPawnMap = new EnumMap<>(ChessColor.class);
        initRowPawnMap.put(ChessColor.WHITE, 2);
        initRowPawnMap.put(ChessColor.BLACK, maxRows - 1);
        return Map.copyOf(initRowPawnMap);
    }
    
    private static Map<ChessColor, Integer> crowningRowMap(int maxRows) {
        Map<ChessColor, Integer> crowningRowMap = new EnumMap<>(ChessColor.class);
        crowningRowMap.put(ChessColor.WHITE, maxRows);
        crowningRowMap.put(ChessColor.BLACK, 1);
        return Map.copyOf(crowningRowMap);
    }
    
    private static Map<CastlingType, Integer> rookInitColMap(int maxCols) {           
        Map<CastlingType, Integer> rookInitColMap = new EnumMap<>(CastlingType.class);
        rookInitColMap.put(CastlingType.LEFT, 1);
        rookInitColMap.put(CastlingType.RIGHT, maxCols);
        return Map.copyOf(rookInitColMap);
    }
    
    private static Map<CastlingType, Integer> kingCastlingColMap(int kingInitCol, int leftMovementWhenCastling, int rightMovementWhenCastling) {
        Map<CastlingType, Integer> kingCastlingColMap = new EnumMap<>(CastlingType.class);
        kingCastlingColMap.put(CastlingType.LEFT, kingInitCol - leftMovementWhenCastling);
        kingCastlingColMap.put(CastlingType.RIGHT, kingInitCol + rightMovementWhenCastling);
        return Map.copyOf(kingCastlingColMap);
    }
    
    private static Map<CastlingType, Integer> rookCastlingColMap(int kingInitCol, int leftMovementWhenCastling, int rightMovementWhenCastling) {
        Map<CastlingType, Integer> rookCastlingColMap = new EnumMap<>(CastlingType.class);
        rookCastlingColMap.put(CastlingType.LEFT, kingInitCol - leftMovementWhenCastling + 1);
        rookCastlingColMap.put(CastlingType.RIGHT, kingInitCol + rightMovementWhenCastling - 1);
        return Map.copyOf(rookCastlingColMap);
    }
    
    public static GameConfiguration standardGame() {
        return standardGame(false, -1);
    }

    public static GameConfiguration standardGame(boolean isTimed) {
        return standardGame(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration standardGame(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 8;
        int kingInitCol = 5;
        int movementWhenCastling = 2;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            // Add Pawns
            IntStream.rangeClosed(1, 8)
                    .forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            // Add Rooks
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(8, initRow), color));
            // Add Knights
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(7, initRow), color));
            // Add Bishops
            pieces.add(new Bishop(Position.of(3, initRow), color));
            pieces.add(new Bishop(Position.of(6, initRow), color));
            // Add Queen
            pieces.add(new Queen(Position.of(4, initRow), color));
            // Add King
            pieces.add(new King(Position.of(5, initRow), color));
        }
        return new GameConfiguration(
            isTimed,
            seconds,
            seconds,
            initRowMap,
            initRowPawnMap,
            crowningRowMap(rows),
            rows,
            cols,
            List.copyOf(pieces),
            kingInitCol,
            rookInitColMap(cols),
            kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
            rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
            new String[] {"Queen", "Knight", "Rook", "Bishop"},
            "STANDARD"
        );
    }

    public static GameConfiguration almostChess() {
        return almostChess(false, -1);
    }

    public static GameConfiguration almostChess(boolean isTimed) {
        return almostChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration almostChess(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 8;
        int kingInitCol = 5;
        int movementWhenCastling = 2;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            // Add Pawns
            IntStream.rangeClosed(1, 8)
                .forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            // Add Rooks
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(8, initRow), color));
            // Add Knights
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(7, initRow), color));
            // Add Bishops
            pieces.add(new Bishop(Position.of(3, initRow), color));
            pieces.add(new Bishop(Position.of(6, initRow), color));
            // Add Chancellor
            pieces.add(new Chancellor(Position.of(4, initRow), color));
            // Add King
            pieces.add(new King(Position.of(5, initRow), color));
        }
        return new GameConfiguration(
            isTimed,
            seconds,
            seconds,
            initRowMap,
            initRowPawnMap,
            crowningRowMap(rows),
            rows,
            cols,
            List.copyOf(pieces),
            kingInitCol,
            rookInitColMap(cols),
            kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
            rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
            new String[] {"Chancellor", "Knight", "Rook", "Bishop"},
            "ALMOSTCHESS"
        );
    }

    public static GameConfiguration capablancaChess() {
        return capablancaChess(false, -1);
    }

    public static GameConfiguration capablancaChess(boolean isTimed) {
        return capablancaChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration capablancaChess(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 10;
        int kingInitCol = 6;
        int movementWhenCastling = 3;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            IntStream.rangeClosed(1, 10).forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(10, initRow), color));
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(9, initRow), color));
            pieces.add(new Bishop(Position.of(4, initRow), color));
            pieces.add(new Bishop(Position.of(7, initRow), color));
            pieces.add(new Chancellor(Position.of(8, initRow), color));
            pieces.add(new ArchBishop(Position.of(3, initRow), color));
            pieces.add(new Queen(Position.of(5, initRow), color));
            pieces.add(new King(Position.of(6, initRow), color));
        }
        return new GameConfiguration(
                isTimed,
                seconds,
                seconds,
                initRowMap,
                initRowPawnMap,
                crowningRowMap(rows),
                rows,
                cols,
                List.copyOf(pieces),
                kingInitCol,
                rookInitColMap(cols),
                kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                new String[] {"Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"},
                "CAPABLANCA"
        );
    }

    public static GameConfiguration gothicChess() {
        return gothicChess(false, -1);
    }

    public static GameConfiguration gothicChess(boolean isTimed) {
        return gothicChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration gothicChess(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 10;
        int kingInitCol = 6;
        int movementWhenCastling = 3;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            IntStream.rangeClosed(1, 10).forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(10, initRow), color));
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(9, initRow), color));
            pieces.add(new Bishop(Position.of(3, initRow), color));
            pieces.add(new Bishop(Position.of(8, initRow), color));
            pieces.add(new Chancellor(Position.of(5, initRow), color));
            pieces.add(new ArchBishop(Position.of(7, initRow), color));
            pieces.add(new Queen(Position.of(4, initRow), color));
            pieces.add(new King(Position.of(6, initRow), color));
        }
        return new GameConfiguration(
                isTimed,
                seconds,
                seconds,
                initRowMap,
                initRowPawnMap,
                crowningRowMap(rows),
                rows,
                cols,
                List.copyOf(pieces),
                kingInitCol,
                rookInitColMap(cols),
                kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                new String[] {"Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"},
                "GOTHIC"
        );
    }

    public static GameConfiguration janusChess() {
        return janusChess(false, -1);
    }

    public static GameConfiguration janusChess(boolean isTimed) {
        return janusChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration janusChess(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 10;
        int kingInitCol = 5;
        int leftMovementWhenCastling = 3;
        int rightMovementWhenCastling = 4;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            IntStream.rangeClosed(1, 10).forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(10, initRow), color));
            pieces.add(new Knight(Position.of(3, initRow), color));
            pieces.add(new Knight(Position.of(8, initRow), color));
            pieces.add(new Bishop(Position.of(4, initRow), color));
            pieces.add(new Bishop(Position.of(7, initRow), color));
            pieces.add(new ArchBishop(Position.of(2, initRow), color));
            pieces.add(new ArchBishop(Position.of(9, initRow), color));
            pieces.add(new Queen(Position.of(6, initRow), color));
            pieces.add(new King(Position.of(5, initRow), color));
        }
        return new GameConfiguration(
                isTimed,
                seconds,
                seconds,
                initRowMap,
                initRowPawnMap,
                crowningRowMap(rows),
                rows,
                cols,
                List.copyOf(pieces),
                kingInitCol,
                rookInitColMap(cols),
                kingCastlingColMap(kingInitCol, leftMovementWhenCastling, rightMovementWhenCastling),
                rookCastlingColMap(kingInitCol, leftMovementWhenCastling, rightMovementWhenCastling),
                new String[] {"Queen", "ArchBishop", "Knight", "Rook", "Bishop"},
                "JANUS"
        );
    }

    public static GameConfiguration modernChess() {
        return modernChess(false, -1);
    }

    public static GameConfiguration modernChess(boolean isTimed) {
        return modernChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration modernChess(boolean isTimed, int seconds) {
        int rows = 9;
        int cols = 9;
        int kingInitCol = 5;
        int movementWhenCastling = 2;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            IntStream.rangeClosed(1, 9).forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(9, initRow), color));
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(8, initRow), color));
            pieces.add(new Bishop(Position.of(3, initRow), color));
            pieces.add(new Bishop(Position.of(7, initRow), color));
            pieces.add(new ArchBishop(Position.of(6, initRow), color));
            pieces.add(new Queen(Position.of(4, initRow), color));
            pieces.add(new King(Position.of(5, initRow), color));
        }
        return new GameConfiguration(
                isTimed,
                seconds,
                seconds,
                initRowMap,
                initRowPawnMap,
                crowningRowMap(rows),
                rows,
                cols,
                List.copyOf(pieces),
                kingInitCol,
                rookInitColMap(cols),
                kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                new String[] {"Queen", "ArchBishop", "Knight", "Rook", "Bishop"},
                "MODERN"
        );
    }

    public static GameConfiguration tuttiFruttiChess() {
        return tuttiFruttiChess(false, -1);
    }

    public static GameConfiguration tuttiFruttiChess(boolean isTimed) {
        return tuttiFruttiChess(isTimed, isTimed ? 300 : -1);
    }

    public static GameConfiguration tuttiFruttiChess(boolean isTimed, int seconds) {
        int rows = 8;
        int cols = 8;
        int kingInitCol = 5;
        int movementWhenCastling = 2;
        Map<ChessColor, Integer> initRowMap = initRowMap(rows);
        Map<ChessColor, Integer> initRowPawnMap = initRowPawnMap(rows);
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRowMap.get(color);
            int initRowPawn = initRowPawnMap.get(color);
            IntStream.rangeClosed(1, 8).forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            pieces.add(new Chancellor(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(8, initRow), color));
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new ArchBishop(Position.of(7, initRow), color));
            pieces.add(new Bishop(Position.of(3, initRow), color));
            pieces.add(new Amazon(Position.of(4, initRow), color));
            pieces.add(new Queen(Position.of(6, initRow), color));
            pieces.add(new King(Position.of(5, initRow), color));
        }
        return new GameConfiguration(
                isTimed,
                seconds,
                seconds,
                initRowMap,
                initRowPawnMap,
                crowningRowMap(rows),
                rows,
                cols,
                List.copyOf(pieces),
                kingInitCol,
                rookInitColMap(cols),
                kingCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                rookCastlingColMap(kingInitCol, movementWhenCastling, movementWhenCastling),
                new String[] {"Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"},
                "TUTTIFRUTTI"
        );
    }

    public int initRow(ChessColor color) {return initRow.get(color);}
    public int initRowPawn(ChessColor color) {return initRowPawn.get(color);}
    public int crowningRow(ChessColor color) {return crowningRow.get(color);}
    public int kingCastlingCol(CastlingType type) {return kingCastlingCol.get(type);}
    public int rookInitCol(CastlingType type) {return rookInitCol.get(type);}
    public int rookCastlingCol(CastlingType type) {return rookCastlingCol.get(type);}
    public Position kingInitPos(ChessColor color) {return Position.of(kingInitCol, initRow(color));}
    public Position kingCastlingPos(ChessColor color, CastlingType type) {return Position.of(kingCastlingCol.get(type), initRow(color));}
    public Position rookInitPos(ChessColor color, CastlingType type) {return Position.of(rookInitCol.get(type), initRow(color));}
    public Position rookCastlingPos(ChessColor color, CastlingType type) {return Position.of(rookCastlingCol.get(type), initRow(color));}
    
}
