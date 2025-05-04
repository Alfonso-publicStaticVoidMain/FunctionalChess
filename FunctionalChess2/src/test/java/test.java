
import functional_chess_model.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 *
 * @author agall
 */
public class test {
    
    static Chess standardGame = Chess.standardGame();
    
    @Test
    void testCheckPieceAt() {
        Chess game = Chess.standardGame();
        
        assertEquals(true, game.checkPieceAt(Position.of(1, 1)));
    }
    
    @Test
    void testFindPieceAt() {
        Chess game = Chess.standardGame();
        
        assertEquals(Optional.of(new Rook(Position.of(1, 1), ChessColor.WHITE)), game.findPieceAt(Position.of(1, 1)));
        assertNotEquals(Optional.of(new Bishop(Position.of(1, 1), ChessColor.WHITE)), game.findPieceAt(Position.of(1, 1)));
    }
    
    // Note to self: assertAll
    /*
    assertAll("person",
        () -> assertEquals("Jane", person.getFirstName()),
        () -> assertEquals("Doe", person.getLastName())
    );
    */
}
