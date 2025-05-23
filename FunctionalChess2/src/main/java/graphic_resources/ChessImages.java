package graphic_resources;

import java.awt.Image;
import java.io.Serializable;
import javax.swing.ImageIcon;

/**
 * Class containing static fields with the icons of each possible piece.
 * @author Alfonso Gallego
 */
public class ChessImages implements Serializable {
    
    // Standard Chess Pieces (White)
    public static final ImageIcon WHITE_KING = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_king.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_QUEEN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_queen.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_ROOK = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_rook.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_BISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_bishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_KNIGHT = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_knight.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_PAWN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_pawn.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    // Nonstandard Chess Pieces (White)
    public static final ImageIcon WHITE_AMAZON = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_amazon.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_CHANCELLOR = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_chancellor.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_ARCHBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_archbishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_MANN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_mann.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITE_NIGHTRIDER = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_nightrider.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    
    // Standard Chess Pieces (Black)
    public static final ImageIcon BLACK_KING = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_king.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_QUEEN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_queen.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_ROOK = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_rook.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_BISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_bishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_KNIGHT = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_knight.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_PAWN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_pawn.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    // Nonstandard Chess Pieces (Black)
    public static final ImageIcon BLACK_AMAZON = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_amazon.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_CHANCELLOR = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_chancellor.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_ARCHBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_archbishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_MANN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_mann.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACK_NIGHTRIDER = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_nightrider.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

}