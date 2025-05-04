package graphic_resources;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;

/**
 *
 * @author Alfonso Gallego
 */
public class Buttons {

    /**
     * Standard template for a JButton, to be followed by the reset, save and
     * load buttons.
     * @param label String to be displayed inside the button.
     * @param actionCommand String to assign to the action command of the
     * button.
     * @return A JButton that:
     * <ul>
     * <li>Is opaque.</li>
     * <li>Does not have a border.</li>
     * <li>Its font is Arial, bold and size 16.</li>
     * <li>Its text and ActionCommand is the label parameter.</li>
     * </ul>
     */
    public static JButton standardButton(String label, String actionCommand) {
        JButton result = new JButton();
        result.setOpaque(true);
        result.setBorderPainted(false);
        result.setFont(new Font("Arial", Font.BOLD, 16));
        result.setText(label);
        result.setActionCommand(actionCommand);
        return result;
    }
    
    /**
     * Overloaded version of {@link Buttons#standardButton(String, String)},
     * defaulting actionCommand to label.
     * @param label
     * @return A button with text and action command label in the style of
     * {@link Buttons#standardButton(String, String)}.
     */
    public static JButton standardButton(String label) {
        return standardButton(label, label);
    }

    public static JButton boardButton() {
        JButton button = new JButton();
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 80));
        return button;
    }
    
}
