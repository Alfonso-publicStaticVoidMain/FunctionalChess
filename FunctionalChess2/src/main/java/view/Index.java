package view;

import configparams.ConfigParameters;
import controller.IndexController;
import functional_chess_model.GameVariant;
import graphic_resources.Buttons;
import graphic_resources.ChessImages;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author Alfonso Gallego
 */
public class Index extends JFrame {
    
    private final JPanel topPanel;
    private final JPanel buttonsPanel;
    private final JLabel title;
    private final JLabel subTitle;
    private final JButton[] buttons;
    private final JCheckBox timerToggleCheckbox;
    private final JPanel checkboxPanel;
    private final JPanel bottomTopPanel;
    public JPanel iconsPanel;
    private JButton newPieces;
    private final JButton exitButton;
    
    private IndexController controller;
    
    public Index() {
        setTitle("Index");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        setLocationRelativeTo(null);
        topPanel = new JPanel(new BorderLayout());

        title = new JLabel("Chess", SwingConstants.CENTER);
        title.setFont(new Font("Garamond", Font.BOLD, 48));

        subTitle = new JLabel("and many variants", SwingConstants.CENTER);
        subTitle.setFont(new Font("Garamond", Font.BOLD, 30));

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(subTitle, BorderLayout.CENTER);

        timerToggleCheckbox = new JCheckBox("Timed Game");
        timerToggleCheckbox.setActionCommand(ConfigParameters.timerToggleActionCommand);
        timerToggleCheckbox.setFont(new Font("Arial", Font.PLAIN, 16));
        timerToggleCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        timerToggleCheckbox.setOpaque(false);

        // Center the checkbox using a wrapper panel
        checkboxPanel = new JPanel();
        checkboxPanel.setOpaque(false);
        checkboxPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        checkboxPanel.add(timerToggleCheckbox);
        
        bottomTopPanel = new JPanel();
        bottomTopPanel.setLayout(new BoxLayout(bottomTopPanel, BoxLayout.Y_AXIS));
        bottomTopPanel.setOpaque(false);
        bottomTopPanel.add(checkboxPanel);
        bottomTopPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        topPanel.add(bottomTopPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttons = new JButton[ConfigParameters.variantNames.length+2];

        for (int i = 0; i < ConfigParameters.variantNames.length; i++) {
            String variant = ConfigParameters.variantNames[i] + " (" + ConfigParameters.variantSizes[i] + ")";
            String variantActionCommand = ConfigParameters.variantNamesUpperCase.get(i);
            // Row panel: one row per button+icons, horizontal layout
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            // Button creation
            JButton button = Buttons.standardButton(variant, variantActionCommand);
            button.setMaximumSize(new Dimension(250, 50)); 
            button.setMinimumSize(new Dimension(250, 50));
            button.setPreferredSize(new Dimension(250, 50));
            button.setBackground(Color.RED);
            button.setAlignmentY(Component.CENTER_ALIGNMENT);
            buttons[i] = button;

            rowPanel.add(button);
            rowPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            // Icons panel (horizontal stack)
            iconsPanel = new JPanel();
            iconsPanel.setLayout(new BoxLayout(iconsPanel, BoxLayout.X_AXIS));
            iconsPanel.setOpaque(false); 
            iconsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

            // Add icons based on variant
            switch (variantActionCommand) {
                case "ALMOSTCHESS" -> iconsPanel.add(new JLabel(ChessImages.WHITECHANCELLOR));
                case "CAPABLANCA", "GOTHIC" -> {
                    iconsPanel.add(new JLabel(ChessImages.WHITECHANCELLOR));
                    iconsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                    iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                }
                case "JANUS" -> iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                case "MODERN" -> iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                case "TUTTIFRUTTI" -> {
                    iconsPanel.add(new JLabel(ChessImages.WHITEAMAZON));
                    iconsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                    iconsPanel.add(new JLabel(ChessImages.WHITECHANCELLOR));
                    iconsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                    iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                }
            }

            rowPanel.add(iconsPanel);

            JPanel rowContainer = new JPanel();
            rowContainer.setLayout(new BoxLayout(rowContainer, BoxLayout.X_AXIS));
            rowContainer.setOpaque(false);

            // Add horizontal margin (e.g. 30px on both sides)
            rowContainer.add(Box.createHorizontalStrut(70));
            rowContainer.add(rowPanel);
            rowContainer.add(Box.createHorizontalStrut(10));

            // Center alignment for rowPanel inside rowContainer
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            buttonsPanel.add(rowContainer);
            buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // vertical space between rows
        }

        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        newPieces = Buttons.standardButton("New Pieces", ConfigParameters.newPiecesButtonActionCommand);
        newPieces.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons[ConfigParameters.variantNames.length] = newPieces;
        buttonsPanel.add(newPieces);
        
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        exitButton = Buttons.standardButton("Exit", ConfigParameters.exitButtonActionCommand);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons[ConfigParameters.variantNames.length+1] = exitButton;
        buttonsPanel.add(exitButton);
        
        add(buttonsPanel, BorderLayout.CENTER);
        add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);
        setVisible(true);
    }
    
    public void setController(IndexController controller) {
        this.controller = controller;
        for (JButton button : buttons) {
            for (ActionListener al : button.getActionListeners()) {
                button.removeActionListener(al);
            }
            button.addActionListener(controller);
        }
        timerToggleCheckbox.addActionListener(controller);
    }
}
