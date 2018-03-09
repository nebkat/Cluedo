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

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.card.Room;

import javax.swing.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TileComponent extends JComponent {
    public static final Color COLOR_CORRIDOR_A = Color.decode("#e4c17f");
    public static final Color COLOR_CORRIDOR_B = Color.decode("#e0c070");

    public static final Color COLOR_ROOM = Color.decode("#ab9e85");
    public static final Color COLOR_PASSAGE = Color.decode("#756e5c");
    public static final Color COLOR_EMPTY = Color.decode("#4f8967");

    private final Tile mTile;

    private Color mDefaultColor;
    private Color mActiveColor;

    private Color mTemporaryDefaultColor;
    private Color mTemporaryActiveColor;

    private boolean mMouseOver = false;

    public TileComponent(Tile tile) {
        super();

        mTile = tile;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setMouseOver(true, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setMouseOver(false, true);
            }
        });

        setOpaque(true);
    }

    public Tile getTile() {
        return mTile;
    }

    public void setColors(Color def, Color active) {
        mDefaultColor = def;
        mActiveColor = active;

        repaint();
    }

    public void setTemporaryColors(Color def, Color active) {
        if (def == mTemporaryDefaultColor && active == mTemporaryActiveColor) return;

        mTemporaryDefaultColor = def;
        mTemporaryActiveColor = active;

        repaint();
    }

    private void setMouseOver(boolean mouseOver, boolean propagate) {
        mMouseOver = mouseOver;

        if (propagate && mTile instanceof RoomTile) {
            ((RoomTile) mTile).getRoom()
                    .getRoomTiles()
                    .forEach((tile) -> tile.getButton().setMouseOver(mouseOver, false));
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isOpaque()) {
            if (mTemporaryDefaultColor == null) {
                g.setColor(!mMouseOver ? mDefaultColor : mActiveColor);
            } else {
                g.setColor(!mMouseOver ? mTemporaryDefaultColor : mTemporaryActiveColor);
            }
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
