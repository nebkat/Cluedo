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
import com.wolfetones.cluedo.game.Player;

import javax.swing.*;
import javax.swing.border.Border;
import java.util.*;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;

public class NotesPanel extends JPanel {

    private Color BACKGROUND_COLOUR = new Color(220, 235, 80);

    public NotesPanel(Player player, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<? extends Card> remainingCards){
        super();

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10),
                        Config.screenRelativeSize(10))));

        setBackground(BACKGROUND_COLOUR);
        // Align the content to the left rather than the center
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel contentFrame = new JPanel();
        contentFrame.setBackground(BACKGROUND_COLOUR);
        contentFrame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        int gridyCounter = 0;

        /*c.fill = GridBagConstraints.HORIZONTAL;
        JButton backButton = new JButton("Back");
        c.gridx = 3;
        c.gridy = gridyCounter++;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        contentFrame.add(backButton,c);*/

        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridyCounter++;
        JLabel suspectsLabel = new JLabel("Suspects");
        suspectsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        contentFrame.add(suspectsLabel, c);

        for (Suspect suspect : suspects) {
            c.insets = new Insets(2, 0, 2, 0);
            c.gridx = 0;
            c.gridy = gridyCounter++;

            contentFrame.add(new JLabel(suspect.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(2, 20, 2, 0);
            if (Math.random() < 0.5) {
                contentFrame.add(new CheckBox("X"), c);
            } else if (remainingCards.contains(suspect)) {
                contentFrame.add(new CheckBox("A"), c);
            } else {
                contentFrame.add(new CheckBox(""),c);
            }
        }

        JLabel weaponsLabel = new JLabel("Weapons");
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridyCounter++;
        weaponsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        contentFrame.add(weaponsLabel, c);

        for (Weapon weapon : weapons) {
            c.insets = new Insets(5, 0, 5, 0);
            c.gridx = 0;
            c.gridy = gridyCounter++;

            contentFrame.add(new JLabel(weapon.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(2, 20, 2, 0);
            if (Math.random() < 0.5) {
                contentFrame.add(new CheckBox("X"), c);
            } else if (remainingCards.contains(weapon)) {
                contentFrame.add(new CheckBox("A"), c);
            } else {
                contentFrame.add(new CheckBox(""),c);
            }
        }

        JLabel roomsLabel = new JLabel("Rooms");
        c.insets = new Insets(2, 0, 2, 0);
        c.gridx = 0;
        c.gridy = gridyCounter++;
        roomsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Config.screenRelativeSize(32)));
        contentFrame.add(roomsLabel, c);

        for (Room room : rooms) {
            c.insets = new Insets(2, 0, 2, 0);
            c.gridx = 0;
            c.gridy = gridyCounter++;

            contentFrame.add(new JLabel(room.getName()), c);

            c.gridx = 1;
            // Padding around the check box
            c.insets = new Insets(2, 20, 2, 0);
            if (Math.random() < 0.5) {
                contentFrame.add(new CheckBox("X"), c);
            } else if (remainingCards.contains(room)) {
                contentFrame.add(new CheckBox("A"), c);
            } else {
                contentFrame.add(new CheckBox(""),c);
            }
        }

        add(contentFrame);
    }
    private class CheckBox extends JPanel {
        private CheckBox(String contents) {
            super();

            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

            setBackground(BACKGROUND_COLOUR);

            add(new JLabel(contents));

            setPreferredSize(new Dimension(15,15));
        }
    }

}