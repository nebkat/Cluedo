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

    public PlayersPanel(List<Player> players, int iconWidth) {
        super();

        setOpaque(false);

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            @SuppressWarnings("SuspiciousNameCombination")
            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(), iconWidth, iconWidth);

            mIcons.put(player, icon);

            c.gridy++;
            c.gridx = 0;
            add(icon, c);

            TextBubble bubble = new TextBubble(icon.getHeight());

            if (i == 0) {
                bubble.setText("I suggest... Professor Plum in the Trophy Room with the Revolver");
            } else if (i == 5) {
                bubble.setText("I have a card");
                bubble.setButton("Choose", null);
            } else if (i == 4) {
                bubble.setText("I was moved to this room, I can make a suggestion");
            } else {
                bubble.setText("I don't have any card!");
            }

            c.gridx = 1;
            add(bubble, c);
        }

        // Push everything to bottom and ensure text bubble column width
        c = new GridBagConstraints();
        c.gridy = 10;
        c.weighty = 1;
        add(Box.createHorizontalStrut(iconWidth), c);
        c.gridx = 1;
        c.weightx = 1;
        add(Box.createHorizontalStrut(0), c);
    }

    public void setActivePlayer(Player player) {
        PlayerIconComponent activeIcon = mIcons.get(player);

        mIcons.values().forEach((i) -> i.setSelected(i == activeIcon));
    }

    public void setTemporarilyActivePlayer(Player player) {
        PlayerIconComponent activeIcon = mIcons.get(player);

        mIcons.values().forEach((i) -> i.setHalfSelected(i == activeIcon));
    }

    public void setPlayerEliminated(Player player) {
        mIcons.get(player).setEliminated();
    }

    private static class PlayerIconComponent extends ScaledImageComponent {
        private boolean mSelected = false;
        private boolean mHalfSelected = false;
        private boolean mEliminated = false;

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

        private void setEliminated() {
            mEliminated = true;
            repaint();
        }

        @Override
        public void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;

            if (mEliminated) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }

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