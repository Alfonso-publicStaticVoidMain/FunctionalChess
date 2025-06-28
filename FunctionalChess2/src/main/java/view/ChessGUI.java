package view;

import configparams.ConfigParameters;
import controller.ChessController;
import functional_chess_model.*;

import graphic_resources.BoardButton;
import graphic_resources.Buttons;
import graphic_resources.EmergentPanels;
import graphic_resources.SquareGridLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
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
    private final BoardButton[][] boardButtons;
    
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

    private JLabel whiteTimer;
    private JLabel blackTimer;
    private Timer gameTimer;
    private final boolean isTimed;
    
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
        
        resetButton = Buttons.standardButton("Reset", ConfigParameters.RESET_BUTTON);
        saveButton = Buttons.standardButton("Save", ConfigParameters.SAVE_BUTTON);
        loadButton = Buttons.standardButton("Load", ConfigParameters.LOAD_BUTTON);
        backButton = Buttons.standardButton("Back", ConfigParameters.BACK_BUTTON);
        
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
        boardButtons = new BoardButton[cols+1][rows+1];
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
     * Sets the argument controller as {@code this} view's controller attribute.
     * @param controller {@link ChessController} to set.
     */
    public void setController(ChessController controller) {
        this.controller = controller;
    }

    public JButton getBackButton() {return backButton;}

    public void addActionListeners() {
        Stream.of(boardButtons)
            .flatMap(Stream::of)
            .forEach(button -> button.addActionListener(controller));
        resetButton.addActionListener(controller);
        saveButton.addActionListener(controller);
        loadButton.addActionListener(controller);
        backButton.addActionListener(controller);
        if (isTimed) {
            gameTimer = controller.viewTimer(whiteTimer, blackTimer);
            gameTimer.start();
            whiteTimer.setText(ChessController.formatTime(controller.getGame().whiteSeconds()));
            blackTimer.setText(ChessController.formatTime(controller.getGame().blackSeconds()));
        }
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
                BoardButton button;

                if (col == 0 && row == 0) {
                    button = BoardButton.blank();
                } else if (row == 0) {
                    button = BoardButton.label(String.valueOf(Position.convertNumberToLetter(col)));
                } else if (col == 0) {
                    button = BoardButton.label(String.valueOf(row));
                } else {
                    button = BoardButton.of(col, row, (col + row + 1) % 2 == 0 ? Color.WHITE : Color.GRAY);
                    button.setFont(new Font("Dialog", Font.PLAIN, 24));
                    button.setActionCommand(ConfigParameters.BOARD_BUTTON);
                }

                boardButtons[col][row] = button;
                boardPanel.add(button);
            }
        }
    }

    /**
     * Paints the cells corresponding to a List of {@link Position}s with
     * a given color during a given amount of time, or indefinitely if
     * inputting a negative time parameter.
     * @param positions List of {@link Position}s to highlight.
     * @param color Color to paint the position's cells with.
     * @param time Time in milliseconds for the painting to endure.
     */
    public void highlightPositions(List<Position> positions, Color color, int time) {
        positions.stream()
            .map(this::getButtonAt)
            .forEach(button -> {
                button.setBackground(color);
                if (time > 0) {
                    Timer timer = new Timer(time, e -> button.resetColor());
                    timer.setRepeats(false);
                    timer.start();
                }
            });
    }

    /**
     * Paints the cells corresponding to a List of {@link Position}s with
     * a given color indefinitely.
     * @param positions List of {@link Position}s to highlight.
     * @param color Color to paint the position's cells with.
     */
    public void highlightPositions(List<Position> positions, Color color) {
        highlightPositions(positions, color, -1);
    }

    /**
     * Highlights with a given color the valid moves of a given {@link Piece}
     * during a given amount of time, or zero if inputting a negative time parameter.
     * @param piece {@link Piece} to check its moves for.
     * @param color Color to paint the board cells with.
     * @param time Time in milliseconds for the painting to endure.
     */
    public void highlightValidMovesOf(Piece piece, Color color, int time) {
        highlightPositions(controller.validMovesOf(piece), color, time);
    }

    /**
     * Highlights with a given color the valid moves of a given {@link Piece} indefinitely.
     * @param piece {@link Piece} to check its moves for.
     * @param color Color to paint the board cells with.
     */
    public void highlightValidMovesOf(Piece piece, Color color) {
        highlightPositions(controller.validMovesOf(piece), color);
    }

    /**
     * Highlights with a given color the moves of a given {@link Piece} that would normally be
     * legal but would cause a check during a given amount of time.
     * @param piece {@link Piece} to check its moves for.
     * @param color Color to paint the board cells with.
     * @param time Time in milliseconds for the painting to endure.
     */
    public void highlightValidMovesThatWouldCauseCheckOf(Piece piece, Color color, int time) {
        highlightPositions(controller.validMovesThatWouldCauseCheckOf(piece), color, time);
    }

    /**
     * Highlights with a given color the moves of a given {@link Piece} that would normally be
     * legal but would cause a check indefinitely.
     * @param piece {@link Piece} to check its moves for.
     * @param color Color to paint the board cells with.
     */
    public void highlightValidMovesThatWouldCauseCheckOf(Piece piece, Color color) {
        highlightPositions(controller.validMovesThatWouldCauseCheckOf(piece), color);
    }

    /**
     * Highlights the board cells containing pieces that could threaten the royal piece of
     * a player after that player moves the given {@link Piece}.
     * @param piece {@link Piece} to move.
     * @param color Color to paint the board cells with.
     * @param time Time in milliseconds for the painting to endure.
     */
    public void highlightPiecesThatCanCaptureKing(Piece piece, Position finPos, Color color, int time) {
        highlightPositions(controller.piecesThatCanCaptureKing(piece, finPos), color, time);
    }

    /**
     * Clears all highlights and paints each board button with its default color.
     */
    public void clearHighlights() {
        Stream.of(boardButtons)
            .flatMap(Stream::of)
            .forEach(BoardButton::resetColor);
    }

    /**
     * Getter for the board button representing a given {@link Position} on the board.
     * @param pos {@link Position} to get the buttom from.
     * @return The {@link BoardButton}
     */
    public BoardButton getButtonAt(Position pos) {
        return boardButtons[pos.x()][pos.y()];
    }

    /**
     * Updates the current state of the board, putting the appropriate icon of
     * the piece present on each board button, or an empty icon if empty.
     */
    public void updateBoard() {
        for (int col = 1; col <= cols; col++) {
            for (int row = 1; row <= rows; row++) {
                Optional<Piece> pieceOrNot = controller.getGame().findPieceAt(Position.of(col, row));
                boardButtons[col][row].setIcon(pieceOrNot.isPresent() ? pieceOrNot.get().toIcon() : new ImageIcon());
            }
        }
    }
    
    /**
     * Updates the active player shown in the active player label.
     * @param str String to show in the label.
     */
    public void updateActivePlayer(String str) {
        activePlayerLabel.setText("Active Player: " + str);
    }

    /**
     * Updates the play history panel with the play passed as parameter,
     * accounting for pieces captured or castling.
     * @param lastPlay {@link Play} to fetch data from.
     */
    public void updatePlayHistory(Play lastPlay) {
        if (lastPlay.pieceCrowned() != null) {
            tableModel.addRow(new Object[] {
                lastPlay.piece().toString() + " => " + lastPlay.pieceCrowned().getClass().getSimpleName(),
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

    /**
     * Shows a message informing the player that they are in checkmate, while
     * also updating the play history panel to reflect that info.
     * @param activePlayer Currently active player.
     */
    public void checkMessage(ChessColor activePlayer) {
        EmergentPanels.informPlayerOkCancel(this, "End of the game", activePlayer+" is in checkmate.\n"+activePlayer.opposite()+" wins.");
        tableModel.addRow(new Object[] {activePlayer.opposite()+" wins.", "---", "---", "---"});
    }

    /**
     * Shows a message informing the players that the game is a draw, while
     * also updating the play history panel to reflect that info.
     * @param activePlayer Currently active player.
     */
    public void drawMessage(ChessColor activePlayer) {
        EmergentPanels.informPlayerOkCancel(this, "End of the game", activePlayer+" isn't in check but every move would cause a check.\nThe game is a draw.");
        tableModel.addRow(new Object[] {"The game is a draw.", "---", "---", "---"});
    }

    /**
     * Resets the play history panel, deleting all info on it
     * about the previous plays.
     */
    public void resetPlayHistory() {
        tableModel.setRowCount(0);
    }

    /**
     * Resets the play history panel, then for each {@link Play}
     * in the game's play history List, updates its info into
     * the panel again.
     */
    public void reloadPlayHistory() {
        resetPlayHistory();
        controller.getGame().playHistory()
            .forEach(this::updatePlayHistory);
    }

}