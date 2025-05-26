package view.online;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

public class ConnectionLogger extends JDialog {
    private final JTextArea logTextArea;

    public ConnectionLogger() {
        setTitle("Connection Log");
        setModal(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        logTextArea = new JTextArea(10, 50);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        add(scrollPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(LocalTime.now() + " - " + message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());

            if (!isVisible()) {
                setVisible(true);
            }
        });
    }

    public void waitAndClose() {
        Timer closeTimer = new Timer(4000, e -> {
            dispose();
        });
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

}