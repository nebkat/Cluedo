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
import com.wolfetones.cluedo.game.PlayerList;
import com.wolfetones.cluedo.ui.component.ScaledImageComponent;
import com.wolfetones.cluedo.ui.component.TextBubble;
import com.wolfetones.cluedo.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.MouseAdapter;
import java.util.List;

public class LogPanel extends JPanel {
    private List<Game.LogEntry> mLog;
    private PlayerList mPlayers;
    private Player mCurrentPlayer;

    public LogPanel(PlayerList players, List<Game.LogEntry> log) {
        super();

        mPlayers = players;
        mLog = log;

        setOpaque(false);
        setLayout(new GridBagLayout());

        // Intercept mouse clicks
        addMouseListener(new MouseAdapter() {});
    }

    public void setCurrentPlayer(Player player) {
        mCurrentPlayer = player;

        update();
    }

    public void update() {
        removeAll();

        for (int i = 0; i < mLog.size(); i++) {
            Game.LogEntry entry = mLog.get(i);

            int tokenSize = Config.screenRelativeSize(35);
            int cardWidth = tokenSize * Card.getCardBackImage().getWidth() / Card.getCardBackImage().getHeight();
            int tooltipHeight = Config.screenRelativeSize(35);

            // Asker
            ScaledImageComponent asker = new ScaledImageComponent(entry.player.getCharacter().getTokenImage(), tokenSize);
            TextBubble.createToolTip(asker, TextBubble.ABOVE, entry.player.getName());
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i;
            c.insets = new Insets(0, 0, Config.screenRelativeSize(2), 0);
            add(asker, c);

            // Suggestion cards
            ScaledImageComponent suggestedSuspect = new ScaledImageComponent(entry.suggestion.suspect.getCardImage(), cardWidth);
            ScaledImageComponent suggestedWeapon = new ScaledImageComponent(entry.suggestion.weapon.getCardImage(), cardWidth);
            ScaledImageComponent suggestedRoom = new ScaledImageComponent(entry.suggestion.room.getCardImage(), cardWidth);

            TextBubble.createToolTip(suggestedSuspect, TextBubble.ABOVE, entry.suggestion.suspect.getName());
            TextBubble.createToolTip(suggestedWeapon, TextBubble.ABOVE, entry.suggestion.weapon.getName());
            TextBubble.createToolTip(suggestedRoom, TextBubble.ABOVE, entry.suggestion.room.getName());

            c = new GridBagConstraints();
            c.gridy = i;
            c.insets = new Insets(0, Config.screenRelativeSize(2), 0, Config.screenRelativeSize(2));
            add(suggestedSuspect, c);
            add(suggestedWeapon, c);
            add(suggestedRoom, c);

            // Fill empty space
            c = new GridBagConstraints();
            c.gridy = i;
            c.weightx = 1.0;
            add(Box.createHorizontalGlue(), c);

            int lastXIndex = 5 + mPlayers.size();

            if (entry.type == Game.LogEntry.Type.FinalAccusation) {
                ScaledImageComponent incorrect = new ScaledImageComponent(ImageUtils.loadImage("icons/accuse.png"), tokenSize);
                TextBubble.createToolTip(incorrect, TextBubble.ABOVE, "Incorrect accusation");
                c = new GridBagConstraints();
                c.gridy = i;
                c.gridx = lastXIndex;
                add(incorrect);

                continue;
            }

            // Responder
            if (entry.responder != null) {
                ScaledImageComponent responder = new ScaledImageComponent(entry.responder.getCharacter().getTokenImage(), tokenSize);
                TextBubble.createToolTip(responder, TextBubble.ABOVE, entry.responder.getName());

                ScaledImageComponent response;
                if (mCurrentPlayer == entry.player) {
                    response = new ScaledImageComponent(entry.response.getCardImage(), cardWidth);
                    TextBubble.createToolTip(response, TextBubble.ABOVE, entry.response.getName());
                } else {
                    response = new ScaledImageComponent(Card.getCardBackImage(), cardWidth);
                    TextBubble.createToolTip(response, TextBubble.ABOVE, "Unknown card");
                }

                c = new GridBagConstraints();
                c.gridy = i;
                c.gridx = lastXIndex;
                add(response, c);
                c.gridx--;
                add(responder, c);
            }

            // Non-responders
            Player player = entry.responder;
            while ((player = mPlayers.getRelative(-1, player)) != entry.player) {
                c.gridx--;

                ScaledImageComponent nonResponder = new ScaledImageComponent(
                        ImageUtils.getColorConvertedImage(player.getCharacter().getTokenImage(), ColorSpace.CS_GRAY),
                        tokenSize
                );
                TextBubble.createToolTip(nonResponder, TextBubble.ABOVE, player.getName());
                add(nonResponder, c);
            }
        }
    }
}

