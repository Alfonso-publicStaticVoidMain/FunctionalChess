package graphic_resources;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

public class EmergentPanels {

    /**
     * Shows an emergent window with a given title and message to inform the user.
     * @param title Title of the window.
     * @param message Message shown in the window.
     */
    public static void informPlayer(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(
            frame,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static int informPlayerOkCancel(JFrame frame, String title, String message) {
        return JOptionPane.showConfirmDialog(
            frame,
            message,
            title,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an emergent window asking for user confirmation with a given message.
     * @param message Message to display.
     * @return true if the player clicked on the OK_OPTION, false otherwise.
     */
    public static boolean askConfirmation(JFrame frame, String message) {
        return informPlayerOkCancel(
            frame,
            "Are you sure you want to do this?",
            message
        ) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows an emergent window letting the user write some text in a line.
     * @param title Title of the window.
     * @return A String containing the text written by the user.
     */
    public static String userTextInputMessage(JFrame frame, String title) throws IOException {
        JTextField textField = new JTextField(20);
        int n = JOptionPane.showConfirmDialog(
            frame,
            textField,
            title,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (n == JOptionPane.OK_OPTION && !textField.getText().isEmpty() && !textField.getText().isBlank()) {
            return textField.getText();
        }
        throw new IOException("Invalid user input");
    }

    /**
     * Shows an emergent window letting the user choose a file.
     * @param startingPath Starting path to be shown.
     * @return The file the user chose.
     * @throws IOException if no file was selected.
     */
    public static File fileChooser(String startingPath) throws IOException {
        JFileChooser fileChooser = new JFileChooser(startingPath);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("DAT files", "dat"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        throw new IOException("No file selected.");
    }

    /**
     * Prints a menu to let the player choose a variant for crowning a Pawn.
     * @param options String array containing the available crowning types.
     * @return A string representing the variant the player wants to crown a
     * Pawn into.
     */
    public static String pawnCrowningMenu(JFrame frame, String[] options) {
        int n = JOptionPane.showOptionDialog(
                frame,
                "You can crown a pawn. What piece do you want to crown your pawn into?\nNot selecting any option will automatically select the first option.",
                "Crowning Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        return options[n];
    }

}
