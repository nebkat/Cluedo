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

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Knowledge;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesPanel extends JPanel {

    private Knowledge mKnowledge;
    private List<Player> mPlayers;
    
    public NotesPanel(Player player, List<Player> players, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms){
        super();

        mKnowledge = player.getKnowledge();
        mPlayers = players;
        
        setOpaque(false);

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        addSection("Suspects", suspects, c);
        addSection("Weapons", weapons, c);
        addSection("Rooms", rooms, c);
    }

    private void addSection(String title, List<? extends Card> cards, GridBagConstraints c){
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridwidth = 6;

        c.gridy++;
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.screenRelativeSize(32)));
        add(sectionLabel, c);

        c.gridwidth = 1;
        for (Player player : mPlayers) {
            c.gridx++;

            add(new PlayerToken(player.getCharacter().getColor(), Config.screenRelativeSize(28)), c);
        }

        c.gridx = 0;

        for (Card card : cards) {
            if (mKnowledge.get().get(card) == null) {
                continue;
            }

            c.anchor = GridBagConstraints.LINE_START;

            c.gridx = 0;
            c.gridy++;

            JLabel cardLabel = new JLabel(card.getName());
            cardLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.screenRelativeSize(20)));
            add(cardLabel, c);

            c.anchor = GridBagConstraints.CENTER;

            for (Map.Entry<Player, Knowledge.Status> entry : mKnowledge.get().get(card).entrySet()) {
                c.gridx++;
                add(new CheckBox(entry.getKey(), entry.getValue(), Config.screenRelativeSize(20)), c);
            }

        }
    }
    private static class PlayerToken extends JComponent {
        private Color mColor;

        private PlayerToken(Color color, int width) {
            super();

            mColor = color;
            setPreferredSize(new Dimension(width, width));
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(mColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class CheckBox extends JComponent {
        private static final double MARGIN = 0.1;
        private static final Map<Knowledge.Value, String> VALUE_ICONS = new HashMap<>() {{
            put(Knowledge.Value.Holding, "holding");
            put(Knowledge.Value.NotHolding, "not-holding");
            put(Knowledge.Value.SuspectedHolding, "suspected-holding");
        }};

        private Knowledge.Status mStatus;
        private Player mPlayer;
        private BufferedImage mImage;

        private CheckBox(Player player, Knowledge.Status status, int width) {
            super();

            mStatus = status;
            mPlayer = player;
            if (status.getValue() != null) {
                mImage = ImageUtils.loadImage("icons/" + VALUE_ICONS.get(status.getValue()) + ".png");
                mImage = ImageUtils.getScaledImage(mImage, (int) (width * (1.0 - 2 * MARGIN)));
            }
            setBorder(BorderFactory.createLineBorder(Color.BLACK, Config.screenRelativeSize(2)));

            setPreferredSize(new Dimension(width, width));
        }

        @Override
        public void paintComponent(Graphics g) {
            if (mImage != null) {
                g.drawImage(mImage, (int) (getWidth() * MARGIN), (int) (getHeight() * MARGIN), null);
            }
        }
    }

}
