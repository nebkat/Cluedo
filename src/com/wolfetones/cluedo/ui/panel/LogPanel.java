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
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Game;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.ui.component.ScaledImageComponent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LogPanel extends JPanel {
    public LogPanel(Player player, List<Game.LogEntry> log) {
        super();

        setOpaque(false);
        setLayout(new GridBagLayout());

        for (int i = 0; i < log.size(); i++) {
            addLine(player, log.get(i), i);
        }

    }

    private void addLine(Player player, Game.LogEntry entry, int suggestionNum) {
        ScaledImageComponent asker = new ScaledImageComponent(entry.player.getCharacter().getTokenImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        ScaledImageComponent suggestedSuspect = new ScaledImageComponent(entry.suggestion.suspect.getTokenImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        ScaledImageComponent suggestedWeapon = new ScaledImageComponent(entry.suggestion.weapon.getTokenImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        ScaledImageComponent suggestedRoom = new ScaledImageComponent(entry.suggestion.room.getCardImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        ScaledImageComponent responder = new ScaledImageComponent(entry.responder.getCharacter().getTokenImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        ScaledImageComponent response;

        if (player.equals(entry.player)) {
            response = new ScaledImageComponent(entry.response.getCardImage(), Config.screenRelativeSize(50), Config.screenRelativeSize(50));
        } else {
            response = new ScaledImageComponent(Card.getCardBackImage(), Config.screenRelativeSize(30), Config.screenRelativeSize(50));
        }

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 2 * suggestionNum;

        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        add(asker, c);

        c.gridy++;
        c.insets = new Insets(0, 0, 0, 5);
        add(suggestedSuspect, c);
        c.gridx++;
        add(suggestedWeapon, c);
        c.gridx++;
        add(suggestedRoom, c);

        c.insets = new Insets(0,0,0,0);
        c.gridx++;
        c.anchor = GridBagConstraints.EAST;
        add(responder, c);
        c.gridx++;
        add(response, c);
    }

}

