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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TileComponent extends JComponent implements MouseListener {
    public static final Color COLOR_CORRIDOR_A = Color.decode("#e4c17f");
    public static final Color COLOR_CORRIDOR_B = Color.decode("#e0c070");

    public static final Color COLOR_ROOM = Color.decode("#ab9e85");
    public static final Color COLOR_PASSAGE = Color.decode("#756e5c");
    public static final Color COLOR_EMPTY = Color.decode("#4f8967");

    private final Tile mTile;

    private Color mDefaultBackgroundColor;
    private Color mActiveBackgroundColor;

    private boolean mMouseOver = false;

    public TileComponent(Tile tile) {
        super();
        addMouseListener(this);
        setOpaque(true);

        mTile = tile;

        if (mTile instanceof CorridorTile || mTile instanceof RoomTile) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public Tile getTile() {
        return mTile;
    }

    public void setBackgroundColors(Color def, Color active) {
        mDefaultBackgroundColor = def;
        mActiveBackgroundColor = active;

        setBackground(!mMouseOver ? def : active);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mMouseOver = true;

        if (mActiveBackgroundColor != null) {
            setBackground(mActiveBackgroundColor);
        }

        if (mTile instanceof RoomTile) {
            ((RoomTile) mTile).getRoom().getRoomTiles().forEach((tile) -> tile.getButton().setMouseOver(true));
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mMouseOver = false;

        if (mActiveBackgroundColor != null) {
            setBackground(mDefaultBackgroundColor);
        }

        if (mTile instanceof RoomTile) {
            ((RoomTile) mTile).getRoom().getRoomTiles().forEach((tile) -> tile.getButton().setMouseOver(false));
        }
    }

    private void setMouseOver(boolean mouseOver) {
        mMouseOver = mouseOver;

        if (mActiveBackgroundColor != null) {
            setBackground(mMouseOver ? mActiveBackgroundColor : mDefaultBackgroundColor);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
