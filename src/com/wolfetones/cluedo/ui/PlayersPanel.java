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

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.Suggestion;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.*;
import java.util.List;

public class PlayersPanel extends JPanel {
    private int mTopPlayer = 0;
    private List<Player> mPlayers;

    private Map<Player, PlayerComponents> mPlayerComponents = new HashMap<>();

    private class PlayerComponents {
        private JPanel panel;
        private PlayerIconComponent icon;
        private TextBubble bubble;
    }

    private Player mActivePlayer;

    private static final double TOKEN_HIGHLIGHT_TOKEN_RATIO = 180.0/170.0;
    private int mTokenHighlightOffset;
    private JComponent mTokenHighlight;

    @SuppressWarnings("SuspiciousNameCombination")
    public PlayersPanel(List<Player> players, int iconWidth) {
        super();

        mPlayers = players;

        setOpaque(false);

        setLayout(new SimpleLayoutManager());

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            PlayerComponents components = new PlayerComponents();

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, Config.screenRelativeSize(2)));
            components.panel = panel;
            panel.setOpaque(false);

            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(), iconWidth, iconWidth);
            components.icon = icon;
            panel.add(icon);

            TextBubble bubble = new TextBubble(icon.getHeight());
            components.bubble = bubble;
            panel.add(bubble);

            panel.setSize(panel.getPreferredSize());
            panel.setLocation(0, i * panel.getHeight());
            add(panel);

            mPlayerComponents.put(player, components);
        }

        mTokenHighlight = new ScaledImageComponent(Util.loadImage("token-highlight.png"),
                (int) (TOKEN_HIGHLIGHT_TOKEN_RATIO * iconWidth),
                (int) (TOKEN_HIGHLIGHT_TOKEN_RATIO * iconWidth));
        mTokenHighlightOffset = (int)((TOKEN_HIGHLIGHT_TOKEN_RATIO - 1.0) * iconWidth / 2);
        mTokenHighlight.setLocation(-mTokenHighlightOffset, -mTokenHighlightOffset + Config.screenRelativeSize(2));

        add(mTokenHighlight);
    }

    public void setTopPlayer(Player topPlayer) {
        mTopPlayer = mPlayers.indexOf(topPlayer);

        for (int i = 0; i < mPlayers.size(); i++) {
            Player player = mPlayers.get(i);

            JPanel panel = mPlayerComponents.get(player).panel;

            int newIndex = (i - mTopPlayer + mPlayers.size()) % mPlayers.size();

            Animator.getInstance().animateAndInterruptAll(panel)
                    .translate(0, newIndex * panel.getHeight())
                    .setDuration(1500)
                    .start();
        }
    }

    public void setActivePlayer(Player player) {
        if (mActivePlayer == player) {
            return;
        }

        int targetX;
        int targetY;

        int initialX = mTokenHighlight.getX();
        int initialY;

        if (player != null) {
            targetX = -mTokenHighlightOffset;
            targetY = mPlayerComponents.get(player).panel.getY() - mTokenHighlightOffset + Config.screenRelativeSize(2);
        } else {
            targetX = -mTokenHighlight.getWidth();
            targetY = mTokenHighlight.getY();
        }

        if (mActivePlayer != null) {
            initialY = mTokenHighlight.getY();
        } else {
            initialY = targetY;
        }

        Animator.getInstance().animateAndInterruptAll(mTokenHighlight)
                .translate(initialX, initialY, targetX, targetY)
                .setDuration(300)
                .start();

        mActivePlayer = player;
    }

    public void setPlayerEliminated(Player player, boolean eliminated) {
        mPlayerComponents.get(player).icon.setEliminated(eliminated);
        mPlayerComponents.get(player).bubble.hideBubble();
    }

    public void showBubble(Player player, String text) {
        TextBubble bubble = mPlayerComponents.get(player).bubble;

        bubble.setText(text);
        bubble.showBubble();
    }

    public void hideBubbles() {
        mPlayerComponents.forEach((key, value) -> value.bubble.hideBubble());
    }

    public void showQuestionResponses(Player poser, Suggestion suggestion, Player cardPlayer, Runnable cardPlayerAction) {
        TextBubble poserBubble = mPlayerComponents.get(poser).bubble;
        poserBubble.setText("I suggest... " + suggestion.asHumanReadableString());
        poserBubble.setButton(null, null);

        poserBubble.showBubble();

        int delayCounter = 1;

        ListIterator<Player> iterator = mPlayers.listIterator((mPlayers.indexOf(poser) + 1) % mPlayers.size());
        Player checkPlayer;
        while ((checkPlayer = iterator.next()) != (cardPlayer == null ? poser : cardPlayer)) {
            TextBubble noCardPlayerBubble = mPlayerComponents.get(checkPlayer).bubble;

            noCardPlayerBubble.setText("I have no cards!");
            noCardPlayerBubble.setButton(null, null);

            noCardPlayerBubble.showBubble(delayCounter++ * 500);

            // Loop around to first player
            if (!iterator.hasNext()) {
                iterator = mPlayers.listIterator();
            }
        }

        if (cardPlayer != null) {
            TextBubble cardPlayerBubble = mPlayerComponents.get(cardPlayer).bubble;

            cardPlayerBubble.setText("I have a card... ");
            cardPlayerBubble.setButton("Show card", cardPlayerAction);

            cardPlayerBubble.showBubble(delayCounter * 500);
        }
    }

    private static class PlayerIconComponent extends ScaledImageComponent {
        private boolean mEliminated = false;

        public PlayerIconComponent(BufferedImage image, int width, int height) {
            super(image, width, height);
        }

        private void setEliminated(boolean eliminated) {
            mEliminated = eliminated;
            repaint();
        }

        @Override
        public void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;

            if (mEliminated) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }

            g.drawImage(mImage, 0, 0, null);
        }
    }
}