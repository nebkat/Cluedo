package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.StartTile;

import javax.swing.*;
import java.awt.*;

/**
 * Colored circle placed behind starting tiles to show which player starts where.
 */
public class StartTileCircle extends JComponent {
    private Color mColor;

    public StartTileCircle(StartTile startTile, int tileSize) {
        mColor = startTile.getStartingSuspect().getColor().darker();

        int x = (int) (((float) startTile.getX() - 0.625f) * tileSize);
        int y = (int) (((float) startTile.getY() - 0.625f) * tileSize);

        int size = (int) (tileSize * 2.25f);

        // Move circle towards the nearest corridor tile
        if (startTile.getLeft() instanceof CorridorTile) {
            x -= tileSize / 4f;
        } else if (startTile.getUp() instanceof CorridorTile) {
            y -= tileSize / 4f;
        } else if (startTile.getRight() instanceof CorridorTile) {
            x += tileSize / 4f;
        } else if (startTile.getDown() instanceof CorridorTile) {
            y += tileSize / 4f;
        }

        setBounds(x, y, size, size);

        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(mColor);
        g.fillOval(0, 0, getWidth(), getHeight());
    }
}
