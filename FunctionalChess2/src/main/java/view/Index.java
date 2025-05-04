package view;

import controller.IndexController;
import graphic_resources.Buttons;
import graphic_resources.ChessImages;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
    private JButton newPieces;
    private final JButton exitButton;
    private static final String[] variantNames = {"Standard Chess", "Almost Chess", "Capablanca Chess", "Gothic Chess", "Janus Chess", "Modern Chess", "Tutti Frutti Chess"};
    private static final String[] variantSizes = {"8x8", "8x8", "8x10", "8x10", "8x10", "9x9", "8x8"};
    
    private IndexController controller;
    
    public Index() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        topPanel = new JPanel(new BorderLayout());

        title = new JLabel("Chess", SwingConstants.CENTER);
        title.setFont(new Font("Garamond", Font.BOLD, 48));

        subTitle = new JLabel("and many variants", SwingConstants.CENTER);
        subTitle.setFont(new Font("Garamond", Font.BOLD, 30));

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(subTitle, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        topPanel.add(Box.createRigidArea(new Dimension(0, 30)), BorderLayout.SOUTH);
        
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttons = new JButton[variantNames.length+2];

        for (int i = 0; i < variantNames.length; i++) {
            String variant = variantNames[i];

            // Row panel: one row per button+icons, horizontal layout
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            // Button creation
            JButton button = Buttons.standardButton(variant + " (" + variantSizes[i] + ")", variant);
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
            switch (variant) {
                case "Almost Chess" -> iconsPanel.add(new JLabel(ChessImages.WHITECHANCELLOR));
                case "Capablanca Chess", "Gothic Chess" -> {
                    iconsPanel.add(new JLabel(ChessImages.WHITECHANCELLOR));
                    iconsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                    iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                }
                case "Janus Chess" -> iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                case "Modern Chess" -> iconsPanel.add(new JLabel(ChessImages.WHITEARCHBISHOP));
                case "Tutti Frutti Chess" -> {
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
        
        newPieces = Buttons.standardButton("New Pieces");
        newPieces.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons[variantNames.length] = newPieces;
        buttonsPanel.add(newPieces);
        
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        exitButton = Buttons.standardButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons[variantNames.length+1] = exitButton;
        buttonsPanel.add(exitButton);
        
        add(buttonsPanel, BorderLayout.CENTER);
        add(Box.createRigidArea(new Dimension(0, 20)), BorderLayout.SOUTH);
        setVisible(true);
    }
    public JPanel iconsPanel;

    public static String[] getVariantNames() {
        return variantNames;
    }
    
    public void setController(IndexController controller) {
        this.controller = controller;
        for (JButton button : buttons) {
            for (ActionListener al : button.getActionListeners()) {
                button.removeActionListener(al);
            }
            button.addActionListener(controller);
        }
        
    }
}
