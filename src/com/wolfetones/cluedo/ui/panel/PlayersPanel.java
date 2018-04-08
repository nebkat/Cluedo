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

package com.wolfetones.cluedo.ui.panel;

import com.wolfetones.cluedo.game.PlayerList;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.Suggestion;
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.ui.component.ImageComponent;
import com.wolfetones.cluedo.ui.SimpleLayoutManager;
import com.wolfetones.cluedo.ui.component.TextBubble;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class PlayersPanel extends JPanel {
    private PlayerList mPlayers;

    private Map<Player, PlayerComponents> mPlayerComponents = new HashMap<>();

    private class PlayerComponents {
        private JPanel panel;
        private PlayerIconComponent icon;
        private TextBubble bubble;
    }

    private int mActivePlayerIndex = -1;

    private int mItemHeight = -1;

    private static final double TOKEN_HIGHLIGHT_TOKEN_RATIO = 180.0/170.0;
    private int mTokenHighlightOffset;
    private JComponent mTokenHighlight;

    public PlayersPanel(PlayerList players, int iconSize) {
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

            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(), iconSize, iconSize);
            components.icon = icon;
            panel.add(icon);

            TextBubble.createToolTip(icon, TextBubble.BELOW, player.getName());

            TextBubble bubble = new TextBubble(TextBubble.RIGHT);
            components.bubble = bubble;
            panel.add(bubble);

            panel.setSize(panel.getPreferredSize());
            panel.setLocation(0, i * panel.getHeight());
            add(panel);

            if (mItemHeight < 0) {
                mItemHeight = panel.getHeight();
            }

            mPlayerComponents.put(player, components);
        }

        mTokenHighlight = new ImageComponent(ImageUtils.loadImage("tokens/token-highlight.png"),
                (int) (TOKEN_HIGHLIGHT_TOKEN_RATIO * iconSize),
                (int) (TOKEN_HIGHLIGHT_TOKEN_RATIO * iconSize));
        mTokenHighlightOffset = (int)((TOKEN_HIGHLIGHT_TOKEN_RATIO - 1.0) * iconSize / 2);
        mTokenHighlight.setLocation(-mTokenHighlightOffset, -mTokenHighlightOffset + Config.screenRelativeSize(2));

        add(mTokenHighlight);
    }

    public void setTopPlayer(Player topPlayer) {
        int topPlayerIndex = mPlayers.indexOf(topPlayer);

        for (int i = 0; i < mPlayers.size(); i++) {
            Player player = mPlayers.get(i);

            JPanel panel = mPlayerComponents.get(player).panel;

            int newIndex = (i - topPlayerIndex + mPlayers.size()) % mPlayers.size();

            Animator.getInstance().animateAndInterruptAll(panel)
                    .translate(0, newIndex * panel.getHeight())
                    .setDuration(1500)
                    .start();
        }
    }

    public void setActivePlayer(int index) {
        int targetX;
        int targetY;

        int initialX = mTokenHighlight.getX();
        int initialY;

        if (index >= 0) {
            targetX = -mTokenHighlightOffset;
            targetY = mItemHeight * index - mTokenHighlightOffset + Config.screenRelativeSize(2);
        } else {
            targetX = -mTokenHighlight.getWidth();
            targetY = mTokenHighlight.getY();
        }

        if (mActivePlayerIndex >= 0) {
            initialY = mTokenHighlight.getY();
        } else {
            initialY = targetY;
        }

        Animator.getInstance().animateAndInterruptAll(mTokenHighlight)
                .translate(initialX, initialY, targetX, targetY)
                .setDuration(300)
                .start();

        mActivePlayerIndex = index;
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

        Iterator<Player> iterator = mPlayers.iteratorStartingAfter(poser);
        while (iterator.hasNext()) {
            Player checkPlayer = iterator.next();
            if (checkPlayer == cardPlayer) {
                break;
            }

            TextBubble noCardPlayerBubble = mPlayerComponents.get(checkPlayer).bubble;

            noCardPlayerBubble.setText("I have no cards!");
            noCardPlayerBubble.setButton(null, null);

            noCardPlayerBubble.showBubble(delayCounter++ * 500);
        }

        if (cardPlayer != null) {
            TextBubble cardPlayerBubble = mPlayerComponents.get(cardPlayer).bubble;

            cardPlayerBubble.setText("I have a card... ");
            cardPlayerBubble.setButton("Show card", cardPlayerAction);

            cardPlayerBubble.showBubble(delayCounter * 500);
        }
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    private static class PlayerIconComponent extends ImageComponent {
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
            Util.setHighQualityRenderingHints(g);

            if (mEliminated) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }

            g.drawImage(mImage, 0, 0, null);
        }
    }
}