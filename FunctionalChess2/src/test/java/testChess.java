
import functional_chess_model.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 *
 * @author Alfonso Gallego
 */
public class testChess {
    
    // Note to self: assertAll
    /*
    assertAll("person",
        () -> assertEquals("Jane", person.getFirstName()),
        () -> assertEquals("Doe", person.getLastName())
    );
    */
    
    static final Chess STANDARDGAME = Chess.standardGame();
    
    @Test
    void testCheckPieceAt() {
        assertEquals(true, STANDARDGAME.checkPieceAt(Position.of(1, 1)));
    } 
    
    @Test
    void testFindPieceAt() {
        assertEquals(Optional.of(new Rook(Position.of(1, 1), ChessColor.WHITE)), STANDARDGAME.findPieceAt(Position.of(1, 1)));
        assertNotEquals(Optional.of(new Bishop(Position.of(1, 1), ChessColor.WHITE)), STANDARDGAME.findPieceAt(Position.of(1, 1)));
    }
    
    @Test
    void testCheckPieceSameOrDiffColorAs() {
        assertEquals(true, STANDARDGAME.checkPieceSameColorAs(Position.of(1, 1), ChessColor.WHITE));
        assertEquals(true, STANDARDGAME.checkPieceSameColorAs(Position.of(8, 8), ChessColor.BLACK));
        
        assertEquals(false, STANDARDGAME.checkPieceDiffColorAs(Position.of(8, 1), ChessColor.WHITE));
        assertEquals(true, STANDARDGAME.checkPieceDiffColorAs(Position.of(1, 2), ChessColor.BLACK));
    }
    
    @Test
    void testFindRoyalPiece() {
        assertEquals(STANDARDGAME.findPieceAt(Position.of(5, 1)), STANDARDGAME.findRoyalPiece(ChessColor.WHITE));
        assertEquals(STANDARDGAME.findPieceAt(Position.of(5, 8)), STANDARDGAME.findRoyalPiece(ChessColor.BLACK));
    }
    
    @Test
    void testPieceCaptured() { 
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(5, 2), Position.of(5, 4))
            .tryToMoveChain(Position.of(4, 7), Position.of(4, 5));
        assertEquals(Optional.of(game.findPieceAt(Position.of(4, 5)).get()), game.pieceCapturedByMove(game.findPieceAt(Position.of(5, 4)).get(), Position.of(4, 5)));
    }
    
    @Test
    void testPieceCapturedEnPassant() {
        Chess game = STANDARDGAME
            .tryToMoveChain(Position.of(5, 2), Position.of(5, 4))
            .tryToMoveChain(Position.of(4, 7), Position.of(4, 5))
            .tryToMoveChain(Position.of(5, 4), Position.of(4, 5))
            .tryToMoveChain(Position.of(5, 7), Position.of(5, 5));
        assertEquals(game.findPieceAt(Position.of(5, 5)), game.pieceCapturedByMove(game.findPieceAt(Position.of(4, 5)).get(), Position.of(5, 6)));
    }
    
    
}
