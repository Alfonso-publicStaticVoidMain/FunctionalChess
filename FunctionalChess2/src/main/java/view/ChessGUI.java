package view;

import controller.ChessController;
import functional_chess_model.*;
import functional_chess_model.Pieces.King;

import graphic_resources.Buttons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * Class to implement a GUI for chess.
 * @author Alfonso Gallego
 * @version 1.0
 */
public class ChessGUI extends JFrame {

    private final JPanel boardPanel;
    private final JButton[][] boardButtons;
    
    private final JPanel topPanel;
    private final JLabel activePlayerLabel;
    private final JButton resetButton;
    private final JButton saveButton;
    private final JButton loadButton;
    private final JButton backButton;
    
    private final JPanel rightPanel;
    private final JTable playHistoryArea;
    private final JPanel tablePanel;
    private final JScrollPane scrollPane;
    private final DefaultTableModel tableModel;    
    
//    private final JPanel leftPanel;
    private JLabel whiteTimer;
    private JLabel blackTimer;
    private Timer gameTimer;
    private boolean isTimed;
    
    private final int rows;
    private final int cols;
    
    private ChessController controller;

    public ChessGUI(int rows, int cols, boolean isTimed) {
        this.isTimed = isTimed;
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100+80*(cols-7), 650+80*(rows-7));
        setLocationRelativeTo(null);
        this.rows = rows;
        this.cols = cols;

        // Top panel - Active player + Save & Reset buttons
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setPreferredSize(new Dimension(250, 40));
        
        activePlayerLabel = new JLabel("Active Player: WHITE", SwingConstants.CENTER);
        activePlayerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        activePlayerLabel.setPreferredSize(new Dimension(250, 30));
        
        resetButton = Buttons.standardButton("Reset");       
        saveButton = Buttons.standardButton("Save");      
        loadButton = Buttons.standardButton("Load");
        backButton = Buttons.standardButton("Back");
        
        topPanel.add(Box.createHorizontalStrut(150));
        topPanel.add(activePlayerLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(resetButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(saveButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(loadButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(backButton);
        topPanel.add(Box.createHorizontalStrut(60));
        
        add(topPanel, BorderLayout.NORTH);
        
        // Right panel - Play History
        rightPanel = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[] {"Piece", "From", "To", "Piece Captured"}, 0);
        playHistoryArea = new JTable(tableModel);
        
        TableColumnModel columnModel = playHistoryArea.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(140); // Piece
        columnModel.getColumn(1).setPreferredWidth(40);  // Init pos
        columnModel.getColumn(2).setPreferredWidth(40);  // Final pos
        columnModel.getColumn(3).setPreferredWidth(140); // Piece captured
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        playHistoryArea.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Init pos
        playHistoryArea.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Final pos
        
        scrollPane = new JScrollPane(playHistoryArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(400, 0));
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Play History", 
            TitledBorder.CENTER, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 16), Color.BLACK));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(tablePanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        
        // Central (board) panel - Chess board
        boardPanel = new JPanel(new SquareGridLayout(rows+1, cols+1));
        boardPanel.setPreferredSize(new Dimension(80*(rows+1), 80*(cols+1)));
        boardPanel.setBounds(0, 0, 80*(rows+1), 80*(cols+1));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        boardButtons = new JButton[cols+1][rows+1];
        initializeBoard();
        add(boardPanel, BorderLayout.CENTER);

        // Left panel - timers
        if (isTimed) {
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setPreferredSize(new Dimension(100, 0));
            whiteTimer = new JLabel("", SwingConstants.CENTER);
            whiteTimer.setFont(new Font("Arial", Font.BOLD, 16));
            whiteTimer.setAlignmentX(Component.RIGHT_ALIGNMENT);
            blackTimer = new JLabel("", SwingConstants.CENTER);
            blackTimer.setFont(new Font("Arial", Font.BOLD, 16));
            blackTimer.setAlignmentX(Component.RIGHT_ALIGNMENT);
            leftPanel.add(Box.createVerticalStrut(25)); // Space from top
            leftPanel.add(blackTimer);
            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(whiteTimer);
            leftPanel.add(Box.createVerticalStrut(85)); // Space at bottom
            add(leftPanel, BorderLayout.WEST);
        }

        setVisible(true);
    }

    /**
     * Sets the argument controller as {@code this} view's controller
     * attribute, and adds it as the action listener of each of its buttons.
     * @param controller {@link ChessController} to set.
     */
    public void setController(ChessController controller) {
        this.controller = controller;
        for (JButton[] buttonArray : boardButtons) {
            for (JButton button : buttonArray) {
                button.addActionListener(this.controller);
            }
        }
        resetButton.addActionListener(this.controller);
        saveButton.addActionListener(this.controller);
        loadButton.addActionListener(this.controller);
        backButton.addActionListener(this.controller);
        if (isTimed) {
            gameTimer = controller.viewTimer();
            gameTimer.start();
            whiteTimer.setText(ChessController.formatTime(controller.getGame().whiteSeconds()));
            blackTimer.setText(ChessController.formatTime(controller.getGame().blackSeconds()));
        }
    }

    public Optional<JLabel> timerForPlayer(ChessColor activePlayer) {
        if (!isTimed) return Optional.empty();
        return Optional.of(activePlayer == ChessColor.WHITE ? whiteTimer : blackTimer);
    }

    /**
     * Initializes the board panel, adding each board button and sets their
     * color, text for the left and lower borders, the action command "Board
     * Button" for the board and the client properties "x" and "y" for tracking
     * their coordinates.
     */
    public void initializeBoard() {
        for (int row = rows; row >= 0; row--) {
            for (int col = 0; col <= cols; col++) {
                JButton button = Buttons.boardButton();
                boardButtons[col][row] = button;
                if (col == 0 && row == 0) {
                    button.setText("");
                    button.setEnabled(false);
                } else if (row == 0) {
                    button.setText(""+ Position.convertNumberToLetter(col));
                    button.setFont(new Font("Arial", Font.BOLD, 16));
                    button.setEnabled(false);
                } else if (col == 0) {
                    button.setText(String.valueOf(row));
                    button.setFont(new Font("Arial", Font.BOLD, 16));
                    button.setEnabled(false);
                } else {
                    // Regular chessboard squares
                    if ((col + row + 1) % 2 == 0) {
                        button.setBackground(Color.WHITE);
                    } else {
                        button.setBackground(Color.GRAY);
                    }
                    button.setFont(new Font("Dialog", Font.PLAIN, 24));
                    button.setActionCommand("Board Button");
                    button.putClientProperty("x", col);
                    button.putClientProperty("y", row);
                }
                boardPanel.add(button);
            }
        }
    }

    /**
     * Colors green the board buttons that the piece can legally move to,
     * and orange the ones that it'd normally be able to but that movement
     * would cause a check.
     * @param piece {@link Piece} to move.
     */
    public void highlightValidMoves(Piece piece) {
        Chess game = controller.getGame();
        for (int col = 1; col <= cols; col++) {
            for (int row = 1; row <= rows; row++) {
                Position potentialMove = Position.of(col, row);
                if (piece.isLegalMovement(game, potentialMove) ||
                    (piece instanceof King && game.castlingTypeOfPlay(piece, potentialMove).isPresent())
                ) {
                    boardButtons[col][row].setBackground(Color.GREEN);
                }
                if (piece.isLegalMovement(game, potentialMove, false) && !piece.isLegalMovement(game, potentialMove, true)) {
                    boardButtons[col][row].setBackground(Color.ORANGE);
                }
            }
        }      
    }
    
    /**
     * Colors yellow during 1 second the board buttons that of the legal moves
     * of a {@link Piece} of the opposing player.
     * @param piece {@link Piece} to move.
     */
    public void highlightMovesOfEnemyPiece(Piece piece) {
        Chess game = controller.getGame();
        for (int col = 1; col <= cols; col++) {
            for (int row = 1; row <= rows; row++) {
                Position potentialMove = Position.of(col, row);
                JButton button = boardButtons[col][row];
                if (piece.isLegalMovement(game, potentialMove)) {
                    Color originalColor = button.getBackground();
                    button.setBackground(Color.YELLOW);
                    button.repaint();
                    Timer timer = new Timer(1000, e -> button.setBackground(originalColor));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        }      
    }
    
    /**
     * Colors red during 1 second the board buttons that contain a {@link Piece}
     * that can capture the King after the parameter pieces moves to the
     * parameter position.
     * @param piece {@link Piece} to move.
     * @param finPos {@link Position} to move it to.
     */
    public void highlightPiecesThatCanCaptureKing(Piece piece, Position finPos) {
        Chess gameAfterMovement = controller.getGame().tryToMoveChain(piece, finPos, false);
        ChessColor color = piece.getColor();
        Optional<Piece> royalPieceOrNot = gameAfterMovement.findRoyalPiece(color);
        if (royalPieceOrNot.isEmpty()) return;
        
        gameAfterMovement.pieces().stream()
            .filter(p -> // Filter for the pieces of a different color than active player that can move to capture active player's King.
                p.getColor() != color &&
                p.isLegalMovement(gameAfterMovement, royalPieceOrNot.get().getPosition(), false)
            )
            .map(p -> boardButtons[p.getPosition().x()][p.getPosition().y()]) // Map each piece to its button on the board
            .forEach(button -> { // Set up a timer on each of those buttons to light it red during 1 second
                Color originalColor = button.getBackground();
                button.setBackground(Color.RED);
                button.repaint();

                Timer timer = new Timer(1000, e -> button.setBackground(originalColor));
                timer.setRepeats(false);
                timer.start();
            });
    }

    /**
     * Clears all highlights and colors each board button with its default color.
     */
    public void clearHighlights() {
        for (int col = 1; col <= cols; col++) {
            for (int row = 1; row <= rows; row++) {
                boardButtons[col][row].setBackground((col + row + 1) % 2 == 0 ? Color.WHITE : Color.GRAY);
            }
        }
    }

    /**
     * Updates the current state of the board, putting the appropiate icon of
     * the piece present on each board button, or an empty icon if empty.
     */
    public void updateBoard() {
        for (int col = 1; col <= cols; col++) {
            for (int row = 1; row <= rows; row++) {
                boardButtons[col][row].setIcon(controller.getGame().checkPieceAt(Position.of(col, row)) ?
                    controller.getGame().findPieceAt(Position.of(col, row)).get().toIcon() :
                    new ImageIcon()
                );
            }
        }
    }
    
    /**
     * Prints a menu to let the player choose a type for crowning a Pawn.
     * @param options String array containing the available crowning types.
     * @return A string representing the type the player wants to crown a
     * Pawn into.
     */
    public String pawnCrowningMenu(String[] options) {
        int n = JOptionPane.showOptionDialog(
            this,
            "You can crown a pawn. What piece do you want to crown your pawn into?\nNot selecting any option will automatically select the first option.",
            "Crowning Menu",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        return options[n];
    }
    
    /**
     * Updates the active player shown in the active player label, fetching
     * the information directly from the game attribute of the controller.
     */
    public void updateActivePlayer() {
        activePlayerLabel.setText("Active Player: "+controller.getGame().activePlayer());
    }
    
    public void updatePlayHistory(Play lastPlay) {
        if (lastPlay.pieceCrowned() != null) {
            tableModel.addRow(new Object[] {
                lastPlay.piece().toString() + " => "+lastPlay.pieceCrowned().getClass().getSimpleName(),
                lastPlay.initPos(),
                lastPlay.finPos(),
                lastPlay.pieceCaptured() != null ? lastPlay.pieceCaptured().toString() : ""
            });
        }
        else if (lastPlay.castlingInfo() != null) {
            tableModel.addRow(new Object[] {
                lastPlay.piece().toString(),
                lastPlay.initPos(),
                lastPlay.finPos(),
                lastPlay.castlingInfo() == CastlingType.LEFT ? "Left Castling" : "Right Castling"
            });
        } else {
            tableModel.addRow(new Object[] {
                lastPlay.piece().toString(),
                lastPlay.initPos(),
                lastPlay.finPos(),
                lastPlay.pieceCaptured() != null ? lastPlay.pieceCaptured().toString() : ""
            });
        }
    }
    
    public void informPlayer(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void checkMessage(ChessColor activePlayer) {
        JOptionPane.showConfirmDialog(
            this,
            activePlayer+" is in checkmate.\n"+activePlayer.opposite()+" wins.",
            "End of the game",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
        tableModel.addRow(new Object[] {activePlayer.opposite()+" wins.", "---", "---", "---"});
    }
    
    public void drawMessage(ChessColor activePlayer) {
        JOptionPane.showConfirmDialog(
            this,
            activePlayer+" isn't in check but every move would cause a check.\nThe game is a draw.",
            "End of the game",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
        tableModel.addRow(new Object[] {"The game is a draw.", "---", "---", "---"});
    }
    
    public boolean areYouSureYouWantToDoThis(String message) {
        return JOptionPane.showConfirmDialog(
            this,
            message,
            "Are you sure you want to do this?",
            JOptionPane.OK_CANCEL_OPTION
        ) == JOptionPane.OK_OPTION;
    }
    
    public String userTextInputMessage(String title) {
        JTextField textField = new JTextField(20);
        int n = JOptionPane.showConfirmDialog(
            this,
            textField,
            title,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (n == JOptionPane.OK_OPTION && !textField.getText().isEmpty() && !textField.getText().isBlank()) {
            return textField.getText();
        } else {
            return ""+controller.getGame().hashCode();
        }
    }
    
    public File fileChooser(String startingPath) throws IOException {
        JFileChooser fileChooser = new JFileChooser(startingPath);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("DAT files", "dat"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        throw new IOException("No file selected.");
    }
    
    public void resetPlayHistory() {
        tableModel.setRowCount(0);
    }
    
    public void reloadPlayHistory() {
        resetPlayHistory();
        for (Play play : controller.getGame().playHistory()) {
            updatePlayHistory(play);
        }
    }
    
    public static class SquareGridLayout implements LayoutManager {
        private final int rows;
        private final int cols;

        public SquareGridLayout(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public void layoutContainer(Container parent) {
            int width = parent.getWidth();
            int height = parent.getHeight();

            // Calculate maximum size that fits both horizontally and vertically
            int squareSize = Math.min(width / cols, height / rows);

            // Calculate total grid size
            int gridWidth = squareSize * cols;
            int gridHeight = squareSize * rows;

            // Center the grid if there's extra space
            int xOffset = (width - gridWidth) / 2;
            int yOffset = (height - gridHeight) / 2;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                int r = i / cols;
                int c = i % cols;

                int x = xOffset + c * squareSize;
                int y = yOffset + r * squareSize;

                parent.getComponent(i).setBounds(x, y, squareSize, squareSize);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(cols * 10, rows * 10);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(cols * 50, rows * 50);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}
    }
}