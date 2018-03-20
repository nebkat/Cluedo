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

package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.Tile;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;

public class TileComponent extends JComponent {
    public static final Color COLOR_CORRIDOR_A = Color.decode("#e4c17f");
    public static final Color COLOR_CORRIDOR_B = Color.decode("#e0c070");

    public static final Color COLOR_ROOM = Color.decode("#ab9e85");
    public static final Color COLOR_PASSAGE = Color.decode("#756e5c");
    public static final Color COLOR_EMPTY = Color.decode("#4f8967");

    private final Tile mTile;

    private Color mTemporaryBackground;

    public TileComponent(Tile tile) {
        super();

        mTile = tile;

        setOpaque(true);
    }

    public Tile getTile() {
        return mTile;
    }

    public void setTemporaryBackground(Color color) {
        mTemporaryBackground = color;

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isOpaque()) {
            if (mTemporaryBackground == null) {
                g.setColor(getBackground());
            } else {
                g.setColor(mTemporaryBackground);
            }
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
