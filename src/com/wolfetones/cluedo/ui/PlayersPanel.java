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

import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayersPanel extends JPanel {
    private Map<Player, PlayerIconComponent> mIcons = new HashMap<>();

    public PlayersPanel(List<Player> players) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBackground(TileComponent.COLOR_EMPTY);

        for (Player player : players) {
            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(),
                    Config.screenRelativeSize(72),
                    Config.screenRelativeSize(72));

            mIcons.put(player, icon);
            add(icon);
        }
    }

    public void setActivePlayer(Player player) {
        PlayerIconComponent activeIcon = mIcons.get(player);

        mIcons.values().forEach((i) -> i.setSelected(i == activeIcon));
    }

    public void setTemporarilyActivePlayer(Player player) {
        PlayerIconComponent activeIcon = mIcons.get(player);

        mIcons.values().forEach((i) -> i.setHalfSelected(i == activeIcon));
    }

    public void removePlayer(Player player) {
        remove(mIcons.remove(player));
    }

    private class PlayerIconComponent extends ScaledImageComponent {
        private boolean mSelected = false;
        private boolean mHalfSelected = false;

        private BufferedImage mFilteredImage;

        private PlayerIconComponent(BufferedImage image, int width, int height) {
            super(image, width, height);

            mFilteredImage = new BufferedImage(
                    mImage.getWidth(), mImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
            op.filter(mImage, mFilteredImage);
        }

        private void setSelected(boolean selected) {
            mSelected = selected;
            repaint();
        }

        private void setHalfSelected(boolean selected) {
            mHalfSelected = selected;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            if (mHalfSelected) {
                int divisions = 9;
                for (int i = 0; i < divisions; i++) {
                    BufferedImage image = i % 2 == 0 ? mImage : mFilteredImage;

                    int x1 = 0;
                    int x2 = getWidth();
                    int y1 = image.getHeight() * i / divisions;
                    int y2 = image.getHeight() * (i + 1) / divisions;

                    g.drawImage(image, x1, y1, x2, y2, x1, y1, x2, y2, null);
                }
            } else {
                g.drawImage(mSelected ? mImage : mFilteredImage, 0, 0, null);
            }
        }
    }
}