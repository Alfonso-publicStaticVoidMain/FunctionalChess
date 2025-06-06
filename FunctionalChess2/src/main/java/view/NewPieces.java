/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import configparams.ConfigParameters;
import controller.NewPiecesController;
import graphic_resources.Buttons;
import javax.swing.*;
import java.awt.*;
import graphic_resources.ChessImages;

public class NewPieces extends JFrame {
    
    private final JPanel piecesPanel;
    private final JPanel amazonPanel;
    private final JPanel archBishopPanel;
    private final JPanel chancellorPanel;
    private final JScrollPane scrollPane;
    private final JPanel buttonPanel;
    private final JButton backButton;
    
    private NewPiecesController controller;

    private static final ImageIcon AMAZON_MOVE = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("newpiecesinfo/amazon_move.png")).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
    private static final ImageIcon ARCHBISHOP_MOVE = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("newpiecesinfo/archbishop_move.png")).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
    private static final ImageIcon CHANCELLOR_MOVE = new ImageIcon(new ImageIcon(ChessImages.class.getClassLoader().getResource("newpiecesinfo/chancellor_move.png")).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));

    public NewPieces() {
        // Basic JFrame setup
        setTitle("Non-Standard Chess Pieces");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        piecesPanel = new JPanel();
        amazonPanel = new JPanel();     
        archBishopPanel = new JPanel();       
        chancellorPanel = new JPanel();
        scrollPane = new JScrollPane(piecesPanel);
        
        // Add initPieces and their descriptions
        addPiecePanel(amazonPanel, "Amazon", ChessImages.WHITE_AMAZON, ChessImages.BLACK_AMAZON,"Moves like a Queen or a Knight.", AMAZON_MOVE);
        addPiecePanel(archBishopPanel, "ArchBishop", ChessImages.WHITE_ARCHBISHOP, ChessImages.BLACK_ARCHBISHOP,"Moves like a Bishop or a Knight.", ARCHBISHOP_MOVE);
        addPiecePanel(chancellorPanel, "Chancellor", ChessImages.WHITE_CHANCELLOR, ChessImages.BLACK_CHANCELLOR,"Moves like a Rook or a Knight.", CHANCELLOR_MOVE);
        add(scrollPane, BorderLayout.CENTER);
        
        buttonPanel = new JPanel();
        backButton = Buttons.standardButton("Back", ConfigParameters.BACK_BUTTON);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }

    private void addPiecePanel(JPanel piecePanel, String name, ImageIcon whiteIcon, ImageIcon blackIcon, String movementDescription, ImageIcon movementDiagram) {
        // Panel for each piece
        piecePanel.setLayout(new BoxLayout(piecePanel, BoxLayout.Y_AXIS));
        piecePanel.setBorder(BorderFactory.createTitledBorder(name));

        // Label for white piece icon
        JLabel whitePieceIconLabel = new JLabel(whiteIcon);
        whitePieceIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label for black piece icon
        JLabel blackPieceIconLabel = new JLabel(blackIcon);
        blackPieceIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label for piece movement description
        JLabel movementDescriptionLabel = new JLabel(movementDescription);
        movementDescriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Label for movement diagram
        JLabel movementDiagramLabel = new JLabel(movementDiagram);
        movementDiagramLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to the panel
        piecePanel.add(whitePieceIconLabel);
        piecePanel.add(Box.createVerticalStrut(10));
        piecePanel.add(blackPieceIconLabel);
        piecePanel.add(Box.createVerticalStrut(10));
        piecePanel.add(movementDescriptionLabel);
        piecePanel.add(Box.createVerticalStrut(10));
        piecePanel.add(movementDiagramLabel);

        // Add the piece panel to the frame
        piecesPanel.add(piecePanel);
    } 

    public void setController(NewPiecesController controller) {
        this.controller = controller;
        backButton.addActionListener(controller);
    }

}

