package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.config.Config;

import javax.swing.border.Border;
import java.awt.*;

public class TileBorder implements Border {
    private final char[] mBordersAndCorners;

    private static final Color COLOR_WALL = Color.decode("#832f32");
    private static final Color COLOR_WINDOW = Color.WHITE;
    private static final Color COLOR_CORRIDOR = Color.decode("#666666");
    private static final Color COLOR_PASSAGE = Color.BLACK;

    private static final int SIZE_WALL = 6;
    private static final int SIZE_WINDOW = 3;
    private static final int SIZE_CORRIDOR = 1;
    private static final int SIZE_PASSAGE = 2;

    private static final int CORNER_TOP_LEFT = 1;
    private static final int CORNER_TOP_RIGHT = 3;
    private static final int CORNER_BOTTOM_RIGHT = 5;
    private static final int CORNER_BOTTOM_LEFT = 7;

    private static final int BORDER_LEFT = 0;
    private static final int BORDER_TOP = 2;
    private static final int BORDER_RIGHT = 4;
    private static final int BORDER_BOTTOM = 6;

    public TileBorder(char[] bordersAndCorners) {
        mBordersAndCorners = bordersAndCorners;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        for (int border = BORDER_LEFT; border <= BORDER_BOTTOM; border += 2) {
            switch (mBordersAndCorners[border]) {
                case Config.Board.Tiles.WALL:
                    g.setColor(COLOR_WALL);
                    paintEdge(g, true, x, y, width, height, SIZE_WALL, border);
                    break;
                case Config.Board.Tiles.WINDOW:
                    g.setColor(COLOR_WINDOW);
                    paintEdge(g, true, x, y, width, height, SIZE_WINDOW, border);
                    g.setColor(COLOR_CORRIDOR);
                    paintEdge(g, false, x, y, width, height, SIZE_WINDOW, border);
                    break;
                case Config.Board.Tiles.CORRIDOR:
                    g.setColor(COLOR_CORRIDOR);
                    paintEdge(g, true, x, y, width, height, SIZE_CORRIDOR, border);
                    break;
                case Config.Board.Tiles.PASSAGE:
                    g.setColor(COLOR_PASSAGE);
                    paintEdge(g, true, x, y, width, height, SIZE_PASSAGE, border);
                    break;
            }
        }

        for (int corner = CORNER_TOP_LEFT; corner <= CORNER_BOTTOM_LEFT; corner+= 2) {
            switch (mBordersAndCorners[corner]) {
                case Config.Board.Tiles.WALL:
                    g.setColor(COLOR_WALL);
                    paintCorner(g, x, y, width, height, SIZE_WALL, corner);
                    break;
            }
        }
    }

    private void paintCorner(Graphics g, int x, int y, int width, int height, int size, int corner) {
        x -= size;
        y -= size;

        if (corner == CORNER_TOP_RIGHT || corner == CORNER_BOTTOM_RIGHT) {
            x += width;
        }
        if (corner == CORNER_BOTTOM_LEFT || corner == CORNER_BOTTOM_RIGHT) {
            y += height;
        }

        g.fillArc(x, y, size * 2, size * 2, -90 * ((corner / 2) + 1), 90);
    }

    private void paintEdge(Graphics g, boolean fill, int x, int y, int width, int height, int size, int border) {
        if (border == BORDER_RIGHT) {
            x += width - size;
        } else if (border == BORDER_BOTTOM) {
            y += height - size;
        }

        boolean vertical = border == BORDER_LEFT || border == BORDER_RIGHT;

        if (!fill) {
            if (border == BORDER_LEFT) {
                x += size;
            } else if (border == BORDER_TOP) {
                y += size;
            }

            size = 1;
        }

        if (fill) {
            g.fillRect(x, y, vertical ? size : width, !vertical ? size : height);
        } else {
            g.drawRect(x, y, vertical ? size : width, !vertical ? size : height);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(5, 5, 5, 5);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
