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
import com.wolfetones.cluedo.ui.component.TileComponent;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.*;
import java.util.List;

public class NotesPanel extends JPanel {
    
    public NotesPanel(Player player, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<? extends Card> remainingCards){
        super();

        Knowledge knowledge = player.getKnowledge();
        
        setOpaque(false);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        int gridYCounter = 0;

        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridYCounter++;
        JLabel suspectsLabel = new JLabel("Suspects");
        suspectsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        add(suspectsLabel, c);

        for (Suspect suspect : suspects) {
            c.insets = new Insets(1, 0, 1, 0);
            c.gridx = 0;
            c.gridy = gridYCounter++;

            add(new JLabel(suspect.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(1, 20, 1, 0);
            if (player.hasCard(suspect)) {
                add(new CheckBox("X"), c);
            } else if (remainingCards.contains(suspect)) {
                add(new CheckBox("A"), c);
            } else {
                add(new CheckBox(""),c);
            }
        }

        JLabel weaponsLabel = new JLabel("Weapons");
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridYCounter++;
        weaponsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        add(weaponsLabel, c);

        for (Weapon weapon : weapons) {
            c.insets = new Insets(1, 0, 1, 0);
            c.gridx = 0;
            c.gridy = gridYCounter++;

            add(new JLabel(weapon.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(1, 20, 1, 0);
            if (player.hasCard(weapon)) {
                add(new CheckBox("X"), c);
            } else if (remainingCards.contains(weapon)) {
                add(new CheckBox("A"), c);
            } else {
                add(new CheckBox(""),c);
            }
        }

        JLabel roomsLabel = new JLabel("Rooms");
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridYCounter++;
        roomsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        add(roomsLabel, c);

        for (Room room : rooms) {
            c.insets = new Insets(1, 0, 1, 0);
            c.gridx = 0;
            c.gridy = gridYCounter++;

            add(new JLabel(room.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(1, 20, 1, 0);
            if (player.hasCard(room)) {
                add(new CheckBox("X"), c);
            } else if (remainingCards.contains(room)) {
                add(new CheckBox("A"), c);
            } else {
                add(new CheckBox(""),c);
            }
        }
    }
    private class CheckBox extends JPanel {
        private CheckBox(String contents) {
            super();

            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

            setOpaque(false);

            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();

            c.insets = new Insets(2, 2, 2, 0);

            add(new JLabel(contents), c);

            setPreferredSize(new Dimension(15,15));
        }
    }

}
