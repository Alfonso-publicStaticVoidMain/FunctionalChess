package view.online;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ConnectionLogger extends JFrame {
    private final JTextArea logTextArea;
    private final JScrollPane scrollPane;

    public ConnectionLogger() {
        setTitle("Connection Log");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        logTextArea = new JTextArea(10, 50);
        logTextArea.setEditable(false);
        scrollPane = new JScrollPane(logTextArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(LocalTime.now().truncatedTo(ChronoUnit.SECONDS) + " - " + message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            if (!isVisible()) setVisible(true);
        });
    }

    public void waitAndClose(int ms) {
        Timer closeTimer = new Timer(ms, e -> dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

    public void waitAndClose() {
        waitAndClose(2000);
    }
}
