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

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;

public class TileComponent extends JComponent {
    public static final Color COLOR_CORRIDOR_A = Color.decode("#e4c17f");
    public static final Color COLOR_CORRIDOR_B = Color.decode("#e0c070");

    public static final Color COLOR_ROOM = Color.decode("#ab9e85");
    public static final Color COLOR_PASSAGE = Color.decode("#756e5c");
    public static final Color COLOR_EMPTY = Color.decode("#4f8967");

    public static final Color COLOR_PATHFINDING_VALID = Color.GREEN.darker();
    public static final Color COLOR_PATHFINDING_VALID_ACTIVE = Color.GREEN;
    public static final Color COLOR_PATHFINDING_INVALID = Color.RED.darker();
    public static final Color COLOR_PATHFINDING_INVALID_ACTIVE = Color.RED;

    private static final Font DOOR_HINT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(20));

    private final Tile mTile;

    private Color mTemporaryBackground;
    private int mDoorHint;

    public TileComponent(Tile tile) {
        super();

        mTile = tile;

        setOpaque(true);
    }

    public Tile getTile() {
        return mTile;
    }

    public void setTemporaryBackground(Color color) {
        if (mTemporaryBackground == color) {
            return;
        }

        mTemporaryBackground = color;

        repaint();
    }

    public void setDoorHint(int hint) {
        mDoorHint = hint;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!isOpaque()) return;

        Util.setHighQualityRenderingHints(g);

        if (mTemporaryBackground == null) {
            g.setColor(getBackground());
        } else {
            g.setColor(mTemporaryBackground);
        }
        g.fillRect(0, 0, getWidth(), getHeight());

        if (mDoorHint > 0) {
            g.setColor(Color.BLACK);
            g.setFont(DOOR_HINT_FONT);
            Util.drawCenteredString(Integer.toString(mDoorHint), 0, 0, getWidth(), getHeight(), g);
        }
    }
}
