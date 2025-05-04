package graphic_resources;

import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Class that containg static fields with the icons of each possible piece.
 * @author Alfonso Gallego
 */
public class ChessImages {
    
    // Standard Chess Pieces (White)
    public static final ImageIcon WHITEKING = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_king.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEQUEEN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_queen.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEROOK = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_rook.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_bishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEKNIGHT = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_knight.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEPAWN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_pawn.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    // Nonstandard Chess Pieces (White)
    public static final ImageIcon WHITEAMAZON = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_amazon.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITECHANCELLOR = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_chancellor.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEARCHBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_archbishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon WHITEMANN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/white_mann.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    // Standard Chess Pieces (Black)
    public static final ImageIcon BLACKKING = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_king.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKQUEEN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_queen.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKROOK = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_rook.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_bishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKKNIGHT = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_knight.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKPAWN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_pawn.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

    // Nonstandard Chess Pieces (Black)
    public static final ImageIcon BLACKAMAZON = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_amazon.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKCHANCELLOR = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_chancellor.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKARCHBISHOP = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_archbishop.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
    public static final ImageIcon BLACKMANN = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("ChessPieces/black_mann.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

}
