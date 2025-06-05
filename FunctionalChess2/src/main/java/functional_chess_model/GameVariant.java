package functional_chess_model;

import controller.ChessController;
import functional_chess_model.Pieces.*;
import functional_chess_model.rules_engine.RulesEngine;
import functional_chess_model.rules_engine.StandardRules;
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

    public static final List<GameVariant> STANDARD_RULES_VARIANTS = List.of(STANDARD, ALMOSTCHESS, CAPABLANCA, GOTHIC, JANUS, MODERN, TUTTIFRUTTI);

    private final int rows;
    private final int cols;
    private final List<Piece> initPieces;
    private final int initKingCol;
    private final int leftCastlingMovement;
    private final int rightCastlingMovement;
    private final String[] crownablePieces;
    private final Map<ChessColor, Map<CastlingType, Boolean>> initCastling;

    public int rows() {return rows;}
    public int cols() {return cols;}
    public List<Piece> initPieces() {return initPieces;}
    public int kingInitCol() {return initKingCol;}
    public int leftCastlingMovement() {return leftCastlingMovement;}
    public int rightCastlingMovement() {return rightCastlingMovement;}
    public String[] crownablePieces() {return crownablePieces;}
    public Map<ChessColor, Map<CastlingType, Boolean>> initCastling() {return initCastling;}

    GameVariant(int rows, int cols, List<Piece> initPieces, boolean castlingEnabled, int initKingCol, int leftCastlingMovement, int rightCastlingMovement) {
        this.rows = rows;
        this.cols = cols;
        this.initPieces = initPieces;
        this.initKingCol = initKingCol;
        this.leftCastlingMovement = leftCastlingMovement;
        this.rightCastlingMovement = rightCastlingMovement;
        this.crownablePieces = initPieces.stream()
            .filter(piece -> !(piece instanceof Pawn) && !(piece instanceof King))
            .map(piece -> piece.getClass().getSimpleName())
            .distinct()
            .toArray(String[]::new);
        this.initCastling = initCastlingWith(castlingEnabled);
    }

    private static Map<PieceType, List<Integer>> standardMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 8),
            PieceType.KNIGHT, List.of(2, 7),
            PieceType.BISHOP, List.of(3, 6),
            PieceType.QUEEN, List.of(4),
            PieceType.KING, List.of(5)
        );
    }

    private static Map<PieceType, List<Integer>> almostChessMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 8),
            PieceType.KNIGHT, List.of(2, 7),
            PieceType.BISHOP, List.of(3, 6),
            PieceType.CHANCELLOR, List.of(4),
            PieceType.KING, List.of(5)
        );
    }

    private static Map<PieceType, List<Integer>> capablancaMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 10),
            PieceType.KNIGHT, List.of(2, 9),
            PieceType.BISHOP, List.of(4, 7),
            PieceType.CHANCELLOR, List.of(8),
            PieceType.ARCHBISHOP, List.of(3),
            PieceType.QUEEN, List.of(5),
            PieceType.KING, List.of(6)
        );
    }

    private static Map<PieceType, List<Integer>> gothicMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 10),
            PieceType.KNIGHT, List.of(2, 9),
            PieceType.BISHOP, List.of(3, 8),
            PieceType.CHANCELLOR, List.of(5),
            PieceType.ARCHBISHOP, List.of(7),
            PieceType.QUEEN, List.of(4),
            PieceType.KING, List.of(6)
        );
    }

    private static Map<PieceType, List<Integer>> janusMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 10),
            PieceType.KNIGHT, List.of(3, 8),
            PieceType.BISHOP, List.of(4, 7),
            PieceType.ARCHBISHOP, List.of(2, 9),
            PieceType.QUEEN, List.of(6),
            PieceType.KING, List.of(5)
        );
    }

    private static Map<PieceType, List<Integer>> modernMapping() {
        return Map.of(
            PieceType.ROOK, List.of(1, 9),
            PieceType.KNIGHT, List.of(2, 8),
            PieceType.BISHOP, List.of(3, 7),
            PieceType.ARCHBISHOP, List.of(6),
            PieceType.QUEEN, List.of(4),
            PieceType.KING, List.of(5)
        );
    }

    private static Map<PieceType, List<Integer>> tuttiFruttiMapping() {
        return Map.of(
            PieceType.CHANCELLOR, List.of(1),
            PieceType.ROOK, List.of(8),
            PieceType.KNIGHT, List.of(2),
            PieceType.ARCHBISHOP, List.of(7),
            PieceType.BISHOP, List.of(3),
            PieceType.AMAZON, List.of(4),
            PieceType.QUEEN, List.of(6),
            PieceType.KING, List.of(5)
        );
    }

    public RulesEngine rules() {
        if (STANDARD_RULES_VARIANTS.contains(this)) {
            return new StandardRules(this);
        }
        throw new IllegalArgumentException("No RulesEngine associated with this variant: "+this);
    }

    public ChessController controller(boolean isTimed) {
        return new ChessController(initGame(isTimed), new ChessGUI(rows, cols, isTimed), rules());
    }

    public ChessController controller(boolean isTimed, boolean isOnline, ChessColor localActivePlayer) {
        return new ChessController(initGame(isTimed), new ChessGUI(rows, cols, isTimed), rules(), isOnline, localActivePlayer);
    }

    public Chess initGame(boolean isTimed, int seconds) {
        return Chess.Builder.blank()
            .withPieces(initPieces)
            .withCastling(initCastling)
            .withPlayHistory(List.of())
            .withActivePlayer(ChessColor.WHITE)
            .withVariant(this)
            .withState(GameState.NOT_STARTED)
            .withIsTimed(isTimed)
            .withSeconds(seconds)
            .build();
    }

    public Chess initGame(boolean isTimed) {
        return initGame(isTimed, isTimed ? 300 : -1);
    }

    public Position initKingPos(ChessColor color) {
        return Position.of(initKingCol, initRow(color));
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
        return side == CastlingType.LEFT ? initKingCol - leftCastlingMovement : initKingCol + rightCastlingMovement;
    }

    public Position castlingKingPos(CastlingType side, ChessColor color) {
        return Position.of(castlingKingCol(side), initRow(color));
    }

    public int castlingRookCol(CastlingType side) {
        return side == CastlingType.LEFT ? initKingCol - leftCastlingMovement + 1 : initKingCol + rightCastlingMovement - 1;
    }

    public Position castlingRookPos(CastlingType side, ChessColor color) {
        return Position.of(castlingRookCol(side), initRow(color));
    }

    private static Map<ChessColor, Map<CastlingType, Boolean>> initCastlingWith(boolean value) {
        Map<ChessColor, Map<CastlingType, Boolean>> initCastling = new EnumMap<>(ChessColor.class);
        for (ChessColor color : ChessColor.values()) {
            Map<CastlingType, Boolean> castlingForColor = new EnumMap<>(CastlingType.class);
            for (CastlingType type : CastlingType.values()) {
                castlingForColor.put(type, value);
            }
            initCastling.put(color, castlingForColor);
        }
        return Map.copyOf(initCastling);
    }

    private static List<Piece> generatePieceList(Map<PieceType, List<Integer>> mapping, int rows, int cols) {
        List<Piece> pieces = new ArrayList<>();
        for (ChessColor color : ChessColor.values()) {
            int initRow = initRow(color, rows);
            int initRowPawn = initRowPawn(color, rows);
            IntStream.rangeClosed(1, cols)
                .forEach(x -> pieces.add(new Pawn(Position.of(x, initRowPawn), color)));
            mapping.keySet()
                .forEach(pieceType -> mapping.get(pieceType).forEach(x -> pieces.add(pieceType.constructor(Position.of(x, initRow), color))));
        }
        return List.copyOf(pieces);
    }

    private static List<Piece> standardPieces() {
        return generatePieceList(standardMapping(), 8, 8);
    }

    private static List<Piece> almostChessPieces() {
        return generatePieceList(almostChessMapping(), 8, 8);
    }

    private static List<Piece> capablancaPieces() {
        return generatePieceList(capablancaMapping(), 8, 10);
    }

    private static List<Piece> gothicPieces() {
        return generatePieceList(gothicMapping(), 8, 10);
    }

    private static List<Piece> janusPieces() {
        return generatePieceList(janusMapping(), 8, 10);
    }

    private static List<Piece> modernPieces() {
        return generatePieceList(modernMapping(), 9, 9);
    }

    private static List<Piece> tuttiFruttiPieces() {
        return generatePieceList(tuttiFruttiMapping(), 8, 8);
    }

}