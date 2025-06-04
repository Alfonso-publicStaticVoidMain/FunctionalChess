package graphic_resources;

import functional_chess_model.Position;

import javax.swing.*;
import java.awt.*;

public class BoardButton extends JButton {
    private int x, y;
    private Position position;
    private Color defaultColor;

    private BoardButton() {
        setOpaque(true);
        setBorderPainted(false);
        setPreferredSize(new Dimension(80, 80));
    }

    public static BoardButton of(int x, int y, Color color) {
        BoardButton button = new BoardButton();
        button.x = x;
        button.y = y;
        button.position = Position.of(x, y);
        button.defaultColor = color;
        button.resetColor();
        return button;
    }

    public static BoardButton of(Position position, Color color) {
        BoardButton button = new BoardButton();
        button.x = position.x();
        button.y = position.y();
        button.position = position;
        button.defaultColor = color;
        button.resetColor();
        return button;
    }

    public static BoardButton blank() {
        BoardButton button = new BoardButton();
        button.setEnabled(false);
        return button;
    }

    public static BoardButton label(String text) {
        BoardButton button = blank();
        button.setText(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        return button;
    }

    public Position getPosition() {
        return position;
    }

    public void resetColor() {
        setBackground(defaultColor);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}
