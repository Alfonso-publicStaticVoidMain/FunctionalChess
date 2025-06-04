package graphic_resources;

import java.awt.*;

public class SquareGridLayout implements LayoutManager {
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
