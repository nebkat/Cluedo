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

import com.wolfetones.cluedo.board.tiles.PassageTile;
import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

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

        if (tile instanceof PassageTile) {
            Room passageRoom = ((PassageTile) tile).getRoom().getPassageRoom();

            int direction = mTile.getY() > Config.Board.HEIGHT / 2 ? TextBubble.BELOW : TextBubble.ABOVE;

            TextBubble.createToolTip(this, direction, "Passage to " + passageRoom.getName());
        }
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
    public void paintComponent(Graphics gg) {
        if (!isOpaque()) return;

        Graphics2D g = (Graphics2D) gg;

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

        if (mTile instanceof PassageTile) {
            Room passageRoom = ((PassageTile) mTile).getRoom().getPassageRoom();

            AffineTransform oldTransform = g.getTransform();

            // Calculate angle to other room
            double angle = Math.atan2(passageRoom.getCenterY() - mTile.getY(), passageRoom.getCenterX() - mTile.getX());
            g.rotate(angle, getWidth() / 2, getHeight() / 2);

            // Draw arrow
            g.drawImage(ImageUtils.getScaledImage(ImageUtils.loadImage("passage-arrow.png"), getWidth()), 0, 0, null);

            // Reset transform
            g.setTransform(oldTransform);
        }
    }
}
