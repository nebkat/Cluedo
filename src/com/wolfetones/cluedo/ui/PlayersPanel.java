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
import com.wolfetones.cluedo.game.Suggestion;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class PlayersPanel extends JPanel {
    private List<Player> mPlayers;

    private Map<Player, PlayerIconComponent> mPlayerIcons = new HashMap<>();
    private Map<Player, TextBubble> mTextBubbles = new HashMap<>();

    public PlayersPanel(List<Player> players, int iconWidth) {
        super();

        mPlayers = players;

        setOpaque(false);

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        for (Player player : players) {
            @SuppressWarnings("SuspiciousNameCombination")
            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(), iconWidth, iconWidth);

            mPlayerIcons.put(player, icon);

            c.gridy++;
            c.gridx = 0;
            add(icon, c);

            TextBubble bubble = new TextBubble(icon.getHeight());

            mTextBubbles.put(player, bubble);

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
        PlayerIconComponent activeIcon = mPlayerIcons.get(player);

        mPlayerIcons.values().forEach((i) -> i.setSelected(i == activeIcon));
    }

    public void setPlayerEliminated(Player player) {
        mPlayerIcons.get(player).setEliminated();
    }

    public void hideQuestionResponses() {
        mTextBubbles.values().forEach(TextBubble::resetBubble);
    }

    public void showQuestionResponses(Player poser, Suggestion suggestion, Player cardPlayer, Runnable cardPlayerAction) {
        TextBubble poserBubble = mTextBubbles.get(poser);
        poserBubble.setText("I suggest... " + suggestion.asHumanReadableString());
        poserBubble.setButton(null, null);

        poserBubble.showBubble();

        int delayCounter = 1;

        ListIterator<Player> iterator = mPlayers.listIterator((mPlayers.indexOf(poser) + 1) % mPlayers.size());
        Player checkPlayer;
        while ((checkPlayer = iterator.next()) != (cardPlayer == null ? poser : cardPlayer)) {
            TextBubble noCardPlayerBubble = mTextBubbles.get(checkPlayer);

            noCardPlayerBubble.setText("I have no cards!");
            noCardPlayerBubble.setButton(null, null);

            noCardPlayerBubble.setDelay(delayCounter++ * 500);
            noCardPlayerBubble.showBubble();

            // Loop around to first player
            if (!iterator.hasNext()) {
                iterator = mPlayers.listIterator();
            }
        }

        if (cardPlayer != null) {
            TextBubble cardPlayerBubble = mTextBubbles.get(cardPlayer);

            cardPlayerBubble.setText("I have a card... ");
            cardPlayerBubble.setButton("Show card", cardPlayerAction);

            cardPlayerBubble.setDelay(delayCounter * 500);
            cardPlayerBubble.showBubble();
        }
    }

    private static class PlayerIconComponent extends ScaledImageComponent {
        private boolean mSelected = false;
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

            g.drawImage(mSelected ? mImage : mFilteredImage, 0, 0, null);
        }
    }
}