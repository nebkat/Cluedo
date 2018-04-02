/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.ui.component;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.StartTile;

import javax.swing.*;
import java.awt.*;

/**
 * Colored circle placed behind starting tiles to show which player starts where.
 */
public class StartTileCircle extends JComponent {
    private static final float SIZE_RELATIVE = 2.25f;

    private Color mColor;

    public StartTileCircle(StartTile startTile, int tileSize) {
        mColor = startTile.getStartingSuspect().getColor().darker();

        int size = (int) (tileSize * SIZE_RELATIVE);

        float offset_relative = (SIZE_RELATIVE - 1f) / 2;
        int x = (int) (((float) startTile.getX() - offset_relative) * tileSize);
        int y = (int) (((float) startTile.getY() - offset_relative) * tileSize);

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
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(mColor);
        g.fillOval(0, 0, getWidth(), getHeight());
    }
}
