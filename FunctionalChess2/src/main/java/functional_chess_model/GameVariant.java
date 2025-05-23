package functional_chess_model;

import controller.ChessController;
import functional_chess_model.Pieces.*;
import view.ChessGUI;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public enum GameVariant {
    STANDARD(8, 8, standardPieces(), true, 5, 2, 2),
    ALMOSTCHESS(8, 8, almostChessPieces(), true, 5, 2, 2),
    CAPABLANCA(8, 10, capablancaPieces(), true, 6, 3, 3),
    GOTHIC(8, 10, gothicPieces(), true, 6, 3, 3),
    JANUS(8, 10, janusPieces(), true, 5, 3, 4),
    MODERN(9, 9, modernPieces(), true, 5, 2, 2),
    TUTTIFRUTTI(8, 8, tuttiFruttiPieces(), true, 5, 2, 2);

    private final int rows;
    private final int cols;
    private final List<Piece> initPieces;
    private final boolean castlingEnabled;
    private final int kingInitCol;
    private final int leftCastlingMovement;
    private final int rightCastlingMovement;
    private final String[] crownablePieces;
    private final Map<ChessColor, Map<CastlingType, Boolean>> initCastling;

    public int rows() {return rows;}
    public int cols() {return cols;}
    public List<Piece> initPieces() {return initPieces;}
    public boolean isCastlingEnabled() {return castlingEnabled;}
    public int kingInitCol() {return kingInitCol;}
    public int leftCastlingMovement() {return leftCastlingMovement;}
    public int rightCastlingMovement() {return rightCastlingMovement;}
    public String[] crownablePieces() {return crownablePieces;}
    public Map<ChessColor, Map<CastlingType, Boolean>> initCastling() {return initCastling;}

    GameVariant(int rows, int cols, List<Piece> initPieces, boolean castlingEnabled, int kingInitCol, int leftCastlingMovement, int rightCastlingMovement) {
        this.rows = rows;
        this.cols = cols;
        this.initPieces = initPieces;
        this.castlingEnabled = castlingEnabled;
        this.kingInitCol = kingInitCol;
        this.leftCastlingMovement = leftCastlingMovement;
        this.rightCastlingMovement = rightCastlingMovement;
        this.crownablePieces = initPieces.stream()
            .filter(piece -> !(piece instanceof Pawn) && !(piece instanceof King))
            .map(piece -> piece.getClass().getSimpleName())
            .distinct()
            .toArray(String[]::new);
        Map<ChessColor, Map<CastlingType, Boolean>> initCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> castlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                castlingForColor.put(type, castlingEnabled);
            }
            initCastling.put(color, castlingForColor);
        }
        this.initCastling = initCastling;
    }

    public ChessController controller(boolean isTimed) {
        return new ChessController(initGame(isTimed), new ChessGUI(rows, cols, isTimed));
    }

    public Chess initGame(boolean isTimed, int seconds) {
        return new Chess(
            List.copyOf(initPieces),
            Map.copyOf(initCastling),
            List.of(),
            ChessColor.WHITE,
            this,
            GameState.NOT_STARTED,
            isTimed,
            seconds,
            seconds
        );
    }

    public Chess initGame(boolean isTimed) {
        return initGame(isTimed, isTimed ? 300 : -1);
    }

    public Position initKingPos(ChessColor color) {
        return Position.of(kingInitCol, initRow(color));
    }

    public static int initRow(ChessColor color, int rows) {
        return color == ChessColor.WHITE ? 1 : rows;
    }

    public int initRow(ChessColor color) {
        return color == ChessColor.WHITE ? 1 : rows;
    }

    public static int initRowPawn(ChessColor color, int rows) {
        return color == ChessColor.WHITE ? 2 : rows - 1;
    }

    public int initRowPawn(ChessColor color) {
        return color == ChessColor.WHITE ? 2 : rows - 1;
    }

    public int crowningRow(ChessColor color) {
        return color == ChessColor.WHITE ? rows : 1;
    }

    public int initRookCol(CastlingType side) {
        return side == CastlingType.LEFT ? 1 : cols;
    }

    public Position initRookPos(CastlingType side, ChessColor color) {
        return Position.of(initRookCol(side), initRow(color));
    }

    public int castlingKingCol(CastlingType side) {
        return side == CastlingType.LEFT ? kingInitCol - leftCastlingMovement : kingInitCol + rightCastlingMovement;
    }

    public Position castlingKingPos(CastlingType side, ChessColor color) {
        return Position.of(castlingKingCol(side), initRow(color));
    }

    public int castlingRookCol(CastlingType side) {
        return side == CastlingType.LEFT ? kingInitCol - leftCastlingMovement + 1 : kingInitCol + rightCastlingMovement - 1;
    }

    public Position castlingRookPos(CastlingType side, ChessColor color) {
        return Position.of(castlingRookCol(side), initRow(color));
    }

    public static List<Piece> standardPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

    public static List<Piece> almostChessPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

    public static List<Piece> capablancaPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

    public static List<Piece> gothicPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

    public static List<Piece> janusPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

    public static List<Piece> modernPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 9);
            int initRowPawn = initRowPawn(color, 9);
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
        return pieces;
    }

    public static List<Piece> tuttiFruttiPieces() {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, 8);
            int initRowPawn = initRowPawn(color, 8);
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
        return pieces;
    }

}
