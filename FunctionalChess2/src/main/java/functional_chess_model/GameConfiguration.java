package functional_chess_model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
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
 * @param kingInitCol Initial column of the King.
 * @param leftRookInitCol Initial column of the left Rook (the piece in that
 * column might not be a Rook!)
 * @param rightRookInitCol Initial column of the right Rook (the piece in that
 * column might not be a Rook!)
 * @param leftCastlingCol Column the King ends up in after doing left castling.
 * @param rightCastlingCol Column the King ends up in after doing right castling.
 * @param crownablePieces Array of piece names that contain all pieces a Pawn
 * can crown into.
 * @param typeOfGame String representing the name of the chess variant being
 * played.
 */
public record GameConfiguration(
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
    
    // TO DO non standard game configs
    public static GameConfiguration standardGame() {
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
            "Standard Chess"
        );
    }
           
    public static GameConfiguration almostChess() {
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
            "Almost Chess"
        );
    }
    
    public static GameConfiguration capablancaChess() {
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
            // Add Pawns
            IntStream.rangeClosed(1, 10)
                .forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            // Add Rooks
            pieces.add(new Rook(Position.of(1, initRow), color));
            pieces.add(new Rook(Position.of(10, initRow), color));
            // Add Knights
            pieces.add(new Knight(Position.of(2, initRow), color));
            pieces.add(new Knight(Position.of(9, initRow), color));
            // Add Bishops
            pieces.add(new Bishop(Position.of(4, initRow), color));
            pieces.add(new Bishop(Position.of(7, initRow), color));
            // Add Chancellor
            pieces.add(new Chancellor(Position.of(8, initRow), color));
            // Add ArchBishop
            pieces.add(new ArchBishop(Position.of(3, initRow), color));
            // Add Queen
            pieces.add(new Queen(Position.of(5, initRow), color));
            // Add King
            pieces.add(new King(Position.of(6, initRow), color));
        }
        return new GameConfiguration(
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
            "Capablanca Chess"
        );
    }
    
//    private static GameConfiguration configFactory(
//        int maxRows,
//        int maxCols,
//        int leftMovementWhenCastling,
//        int rightMovementWhenCastling,
//        int kingInitCol,
//        List<Piece> pieces,
//        String[] crownablePieces,
//        String typeOfGame
//    ) {
//        Map<ChessColor, Integer> initRowMap = new EnumMap<>(ChessColor.class);
//        initRowMap.put(ChessColor.WHITE, 1);
//        initRowMap.put(ChessColor.BLACK, maxRows);
//        
//        Map<ChessColor, Integer> initRowPawnMap = new EnumMap<>(ChessColor.class);
//        initRowPawnMap.put(ChessColor.WHITE, 2);
//        initRowPawnMap.put(ChessColor.BLACK, maxRows - 1);
//        
//        Map<ChessColor, Integer> crowningRowMap = new EnumMap<>(ChessColor.class);
//        crowningRowMap.put(ChessColor.WHITE, maxRows);
//        crowningRowMap.put(ChessColor.BLACK, 1);
//        
//        Map<CastlingType, Integer> rookInitColMap = new EnumMap<>(CastlingType.class);
//        rookInitColMap.put(CastlingType.LEFT, 1);
//        rookInitColMap.put(CastlingType.RIGHT, maxCols);
//        
//        Map<CastlingType, Integer> kingCastlingColMap = new EnumMap<>(CastlingType.class);
//        kingCastlingColMap.put(CastlingType.LEFT, kingInitCol - leftMovementWhenCastling);
//        kingCastlingColMap.put(CastlingType.RIGHT, kingInitCol + rightMovementWhenCastling);
//        
//        Map<CastlingType, Integer> rookCastlingColMap = new EnumMap<>(CastlingType.class);
//        rookCastlingColMap.put(CastlingType.LEFT, kingInitCol - leftMovementWhenCastling + 1);
//        rookCastlingColMap.put(CastlingType.RIGHT, kingInitCol + rightMovementWhenCastling - 1);
//        
//        return new GameConfiguration(initRowMap, initRowPawnMap, crowningRowMap, maxRows, maxCols, pieces, kingInitCol, rookInitColMap, kingCastlingColMap, rookCastlingColMap, crownablePieces, typeOfGame);
//    }
//    
//    public static GameConfiguration standardGameConfig = configFactory(8, 8, 2, 2, 5, new String[] {"Queen", "Knight", "Rook", "Bishop"}, "Standard Chess");
//    public static GameConfiguration almostChessConfig = configFactory(8, 8, 2, 2, 5, new String[] {"Chancellor", "Knight", "Rook", "Bishop"}, "Almost Chess");
//    public static GameConfiguration capablancaConfig = configFactory(8, 10, 3, 3, 6, new String[] {"Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"}, "Capablanca Chess");
//    public static GameConfiguration gothicConfig = configFactory(8, 10, 3, 3, 6, new String[] {"Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"}, "Gothic Chess");
//    public static GameConfiguration janusConfig = configFactory(8, 10, 3, 4, 5, new String[] {"Queen", "ArchBishop", "Knight", "Rook", "Bishop"}, "Janus Chess");
//    public static GameConfiguration modernConfig = configFactory(9, 9, 2, 2, 5, new String[] {"Queen", "ArchBishop", "Knight", "Rook", "Bishop"}, "Modern Chess");
//    public static GameConfiguration tuttiFruttiConfig = configFactory(8, 8, 2, 2, 5, new String[] {"Amazon", "Queen", "Chancellor", "ArchBishop", "Knight", "Rook", "Bishop"}, "Tutti Frutti Chess");
//    
    public int initRow(ChessColor color) {
        return initRow.get(color);
    }
    
    public int initRowPawn(ChessColor color) {
        return initRowPawn.get(color);
    }
    
    public int crowningRow(ChessColor color) {
        return crowningRow.get(color);
    }
    
    public int kingCastlingCol(CastlingType type) {
        return kingCastlingCol.get(type);
    }
    
    public int rookInitCol(CastlingType type) {
        return rookInitCol.get(type);
    }
    
    public int rookCastlingCol(CastlingType type) {
        return rookCastlingCol.get(type);
    }
    
    public Position kingInitPos(ChessColor color) {
        return Position.of(kingInitCol, initRow(color));
    }
    
    public Position kingCastlingPos(ChessColor color, CastlingType type) {
        return Position.of(kingCastlingCol.get(type), initRow(color));
    }
    
    public Position rookInitPos(ChessColor color, CastlingType type) {
        return Position.of(rookInitCol.get(type), initRow(color));
    }
    
    public Position rookCastlingPos(ChessColor color, CastlingType type) {
        return Position.of(rookCastlingCol.get(type), initRow(color));
    }
    
}
