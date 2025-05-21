# FunctionalChess
Functional programming implementation of Chess

**Chess** is the main class in the project, which holds all info about the current state of the game within its attributes:
* List<Piece> pieces: List of all the pieces on the board.
* Map<ChessColor, Map<CastlingType, Boolean>> castling: A "dictionary" mapping each pair of color (black or white) and castling type (left or right) to a boolean representing if that player can still perform that kind of castling.
* List<Play> playHistory: List of all the Plays done in the game thus far.
* ChessColor activePlayer: Color of the active player.
* GameConfiguration config: Configuration attribute storing certain qualities of the game such as number of rows and columns of the board, the starting positions of the pieces, how much the King moves when castling, etc.
* GameState state: Current state of the game, with possible values: NOT_STARTED, IN_PROGRESS, DRAW, WHITE_WINS, BLACK_WINS.

This class is a record class to guarantee immutability. Each time the state changes, a new Chess object is created with the updated values of each of its attributes. Each List and Map attribute of it is also passed before that to the .copyOf method of its class to ensure an immutable List or Map is received by the constructor.

To update its state, Chess has multiple methods that perform the following actions:
* *tryToMove*: Performs a movement.
* *tryToCastle*: Performs castling.
* *checkMate*: Updates the game state, possibly finishing the game.
* *crownPawn*: Crowns a Pawn into another Piece.

Each of this methods returns an object of Optional<Chess>, returning Optional.empty if the game hasn't been modified, because the action was illegal or it resulted in no changes. There's also an analogous method adding "Chain" to the end of the name of each of these that always returns a Chess object, being "this" if the non-chainable method returned Optional.empty, or the object stored in the result otherwise. This kinds of methods can be applied one after another to perform multiple state updates of the game.

**Piece** is an abstract class with attributes Position position, ChessColor color and boolean isRoyal, representing where it is on the board, the player who controls it and if it's a royal piece (by default only the King is royal, but some variants I might implement in the future include the possibility of other kinds of pieces being the one the player needs to maintain alive at all costs). It has abstract methods:
* boolean isLegalMovement: Checks if a proposed movement is valid.
* Piece moveTo: Returns a piece with the same attributes and class but in a new Position.
* ImageIcon toIcon: Returns the ImageIcon representing the piece, stored within the ChessImages class.

