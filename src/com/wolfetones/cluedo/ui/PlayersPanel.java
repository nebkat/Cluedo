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
import java.util.*;
import java.util.List;

public class PlayersPanel extends JPanel {
    private List<Player> mPlayers;

    private Map<Player, PlayerComponents> mPlayerComponents = new HashMap<>();

    private class PlayerComponents {
        private JPanel panel;
        private PlayerIconComponent icon;
        private TextBubble bubble;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public PlayersPanel(List<Player> players, int iconWidth, int panelWidth) {
        super();

        mPlayers = players;

        setOpaque(false);

        setLayout(null);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            PlayerComponents components = new PlayerComponents();

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            components.panel = panel;
            panel.setOpaque(false);

            PlayerIconComponent icon = new PlayerIconComponent(player.getCharacter().getTokenImage(), iconWidth, iconWidth);
            components.icon = icon;
            panel.add(icon);

            TextBubble bubble = new TextBubble(icon.getHeight());
            components.bubble = bubble;
            panel.add(bubble);

            panel.setLocation(0, i * iconWidth);
            panel.setSize(panelWidth, iconWidth);
            add(panel);

            mPlayerComponents.put(player, components);
        }
    }

    public void rearrangePlayers(List<Player> players) {
        mPlayers = players;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            JPanel panel = mPlayerComponents.get(player).panel;

            Animator.getInstance().animateAndInterruptAll(panel)
                    .translate(0, i * panel.getHeight())
                    .setDuration(1500)
                    .start();
        }
    }

    public void setActivePlayers(List<Player> players) {
        mPlayerComponents.forEach((key, value) -> value.icon.setSelected(players.contains(key)));
    }

    public void setActivePlayer(Player player) {
        mPlayerComponents.forEach((key, value) -> value.icon.setSelected(player == null || key == player));
    }

    public void setPlayerEliminated(Player player, boolean eliminated) {
        mPlayerComponents.get(player).icon.setEliminated(eliminated);
    }

    public void showDiceRollResult(Player player, int roll) {
        TextBubble bubble = mPlayerComponents.get(player).bubble;

        bubble.setText("I rolled " + roll);
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

        private void setEliminated(boolean eliminated) {
            mEliminated = eliminated;
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