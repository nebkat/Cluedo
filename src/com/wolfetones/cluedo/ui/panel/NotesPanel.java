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
    
    public NotesPanel(Player player, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<? extends Card> remainingCards){
        super();

        mKnowledge = player.getKnowledge();
        
        setOpaque(false);


        setBorder(BorderFactory.createEmptyBorder(
                0, Config.screenRelativeSize(20),
                0, Config.screenRelativeSize(20)));
        setLayout(new GridBagLayout());


        GridBagConstraints c = new GridBagConstraints();

        addSection("Suspects", suspects, c);
        addSection("Weapons", weapons, c);
        addSection("Rooms", rooms, c);
    }

    private void addSection(String title, List<? extends Card> cards, GridBagConstraints c){
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridwidth = 6;

        c.gridy++;
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.screenRelativeSize(32)));
        add(sectionLabel, c);

        c.gridwidth = 1;
        for (Card card : cards) {
            c.insets = new Insets(1, 0, 1, 0);
            c.gridx = 0;
            c.gridy++;


            JLabel cardLabel = new JLabel(card.getName());
            cardLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(16)));
            add(cardLabel, c);

            c.insets = new Insets(1, 20, 1, 0);
            for (Map.Entry<Player, Knowledge.Status> entry : mKnowledge.get().get(card).entrySet()) {
                c.gridx++;
                // Padding around the check box

                add(new CheckBox(entry.getKey(), entry.getValue(), Config.screenRelativeSize(16)), c);
            }
        }
    }
    private static class CheckBox extends JComponent {
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
                mImage = ImageUtils.loadImage("resources/" + VALUE_ICONS.get(status.getValue()) + ".png");
                mImage = ImageUtils.getScaledImage(mImage, width);
            }
            setBorder(BorderFactory.createLineBorder(Color.BLACK, Config.screenRelativeSize(2)));

            setPreferredSize(new Dimension(width, width));
        }

        @Override
        public void paintComponent(Graphics g) {
            if (mImage != null) {
                g.drawImage(mImage, 0, 0, null);
            }
        }
    }

}
