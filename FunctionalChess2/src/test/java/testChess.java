
import controller.ChessController;
import functional_chess_model.*;
import java.util.List;
import java.util.Optional;
import javax.swing.SwingUtilities;

import functional_chess_model.Pieces.Bishop;
import functional_chess_model.Pieces.Nightrider;
import functional_chess_model.Pieces.Rook;
import functional_chess_model.rules_engine.RulesEngine;
import org.junit.jupiter.api.Test;
import view.ChessGUI;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Alfonso Gallego
 */
public class testChess {
    
    static final Chess STANDARDGAME = GameVariant.STANDARD.initGame(false);
    static final RulesEngine STANDARD_RULES = GameVariant.STANDARD.rules();
    
    @Test
    void testCheckPieceAt() {
        assertTrue(STANDARDGAME.checkPieceAt(Position.of(1, 1)));
    } 
    
    @Test
    void testFindPieceAt() {
        assertEquals(Optional.of(new Rook(Position.of(1, 1), ChessColor.WHITE)), STANDARDGAME.findPieceAt(Position.of(1, 1)));
        assertNotEquals(Optional.of(new Bishop(Position.of(1, 1), ChessColor.WHITE)), STANDARDGAME.findPieceAt(Position.of(1, 1)));
    }
    
    @Test
    void testCheckPieceSameOrDiffColorAs() {
        assertTrue(STANDARDGAME.checkPieceSameColorAs(Position.of(1, 1), ChessColor.WHITE));
        assertTrue(STANDARDGAME.checkPieceSameColorAs(Position.of(8, 8), ChessColor.BLACK));

        assertFalse(STANDARDGAME.checkPieceDiffColorAs(Position.of(8, 1), ChessColor.WHITE));
        assertTrue(STANDARDGAME.checkPieceDiffColorAs(Position.of(1, 2), ChessColor.BLACK));
    }
    
    @Test
    void testFindRoyalPiece() {
        assertEquals(STANDARDGAME.findPieceAt(Position.of(5, 1)), STANDARDGAME.findRoyalPiece(ChessColor.WHITE));
        assertEquals(STANDARDGAME.findPieceAt(Position.of(5, 8)), STANDARDGAME.findRoyalPiece(ChessColor.BLACK));
    }
    
    @Test
    void testPieceCaptured() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(5, 2), Position.of(5, 4), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 7), Position.of(4, 5), STANDARD_RULES);
        assertEquals(Optional.of(game.findPieceAt(Position.of(4, 5)).get()), STANDARD_RULES.pieceCapturedByMove(game, game.findPieceAt(Position.of(5, 4)).get(), Position.of(4, 5)));
    }
    
    @Test
    void testPieceCapturedEnPassant() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(5, 2), Position.of(5, 4), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 7), Position.of(4, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(5, 4), Position.of(4, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(5, 7), Position.of(5, 5), STANDARD_RULES);
        assertEquals(game.findPieceAt(Position.of(5, 5)), STANDARD_RULES.pieceCapturedByMove(game, game.findPieceAt(Position.of(4, 5)).get(), Position.of(5, 6)));
    }
    
    @Test
    void testCastlingTypeOfPlay() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(4, 2), Position.of(4, 3), STANDARD_RULES)
            .tryToMoveChain(Position.of(3, 1), Position.of(5, 3), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 1), Position.of(4, 2), STANDARD_RULES)
            .tryToMoveChain(Position.of(2, 1), Position.of(3, 3), STANDARD_RULES);
        assertEquals(Optional.of(CastlingType.LEFT), STANDARD_RULES.castlingTypeOfPlay(game, Position.of(5, 1), Position.of(3, 1)));
    }
    
    @Test
    void testIsPlayerInCheck() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(4, 2), Position.of(4, 4), STANDARD_RULES)
            .tryToMoveChain(Position.of(5, 7), Position.of(5, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 4), Position.of(5, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(6, 8), Position.of(2, 4), STANDARD_RULES);
        assertTrue(STANDARD_RULES.isPlayerInCheck(game, ChessColor.WHITE));
    }
    
    @Test
    void testCheckConsiderationsWhenMoving() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(4, 2), Position.of(4, 4), STANDARD_RULES)
            .tryToMoveChain(Position.of(5, 7), Position.of(5, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 4), Position.of(5, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(6, 8), Position.of(2, 4), STANDARD_RULES);
        assertFalse(game.findPieceAt(Position.of(2, 1)).get().canMove(game, Position.of(1, 3)));
        game = game.tryToMoveChain(Position.of(2, 1), Position.of(1, 3), false, STANDARD_RULES);
        assertTrue(STANDARD_RULES.isPlayerInCheck(game, ChessColor.WHITE));
    }
    
    @Test
    void testCheckMate() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(6, 2), Position.of(6, 3), STANDARD_RULES)
            .tryToMoveChain(Position.of(5, 7), Position.of(5, 5), STANDARD_RULES)
            .tryToMoveChain(Position.of(7, 2), Position.of(7, 4), STANDARD_RULES)
            .tryToMoveChain(Position.of(4, 8), Position.of(8, 4), STANDARD_RULES);
        assertEquals(GameState.BLACK_WINS, game.checkMateChain(ChessColor.WHITE, STANDARD_RULES).state());
    }
    
    static Chess createTestGameWithPiece(Piece piece) {
        return new Chess(
            List.of(piece),
            GameVariant.STANDARD.initCastling(),
            List.of(),
            ChessColor.WHITE,
            GameVariant.STANDARD,
            GameState.NOT_STARTED,
            false,
            -1,
            -1
        );
    }
    
    public static void main(String[] args) {
        Chess game = createTestGameWithPiece(new Nightrider(Position.of(1,1), ChessColor.WHITE));
        SwingUtilities.invokeLater(() -> new ChessController(game, new ChessGUI(8, 8, false), STANDARD_RULES));
    }
}
