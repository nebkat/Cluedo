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
import com.wolfetones.cluedo.ui.component.ImageComponent;
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

        for (int i = Math.max(0, mLog.size() - 24); i < mLog.size(); i++) {
            Game.LogEntry entry = mLog.get(i);

            int tokenSize = Config.screenRelativeSize(35);
            int cardWidth = tokenSize * Card.getCardBackImage().getWidth() / Card.getCardBackImage().getHeight();

            // Asker
            ImageComponent asker = new ImageComponent(entry.getPlayer().getCharacter().getTokenImage(), tokenSize);
            TextBubble.createToolTip(asker, TextBubble.ABOVE, entry.getPlayer().getName() + " suggested");
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i;
            c.insets = new Insets(0, 0, Config.screenRelativeSize(2), 0);
            add(asker, c);

            // Suggestion cards
            ImageComponent suggestedSuspect = new ImageComponent(entry.getSuggestion().suspect.getCardImage(), cardWidth);
            ImageComponent suggestedWeapon = new ImageComponent(entry.getSuggestion().weapon.getCardImage(), cardWidth);
            ImageComponent suggestedRoom = new ImageComponent(entry.getSuggestion().room.getCardImage(), cardWidth);

            TextBubble.createToolTip(suggestedSuspect, TextBubble.ABOVE, entry.getSuggestion().suspect.getName());
            TextBubble.createToolTip(suggestedWeapon, TextBubble.ABOVE, entry.getSuggestion().weapon.getName());
            TextBubble.createToolTip(suggestedRoom, TextBubble.ABOVE, entry.getSuggestion().room.getName());

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

            if (entry.getType() == Game.LogEntry.Type.FinalAccusation) {
                ImageComponent incorrect = new ImageComponent(ImageUtils.loadImage("icons/accuse.png"), tokenSize);
                TextBubble.createToolTip(incorrect, TextBubble.ABOVE, entry.isCorrect() ? "Correct accusation" : "Incorrect accusation");
                c = new GridBagConstraints();
                c.gridy = i;
                c.gridx = lastXIndex;
                add(incorrect, c);

                continue;
            }

            // Responder
            if (entry.getResponder() != null) {
                ImageComponent responder = new ImageComponent(entry.getResponder().getCharacter().getTokenImage(), tokenSize);
                TextBubble.createToolTip(responder, TextBubble.ABOVE, entry.getResponder().getName() + " responded");

                ImageComponent response;
                if (mCurrentPlayer == entry.getPlayer()) {
                    response = new ImageComponent(entry.getResponse().getCardImage(), cardWidth);
                    TextBubble.createToolTip(response, TextBubble.ABOVE, entry.getResponse().getName());
                } else {
                    response = new ImageComponent(Card.getCardBackImage(), cardWidth);
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
            Player player = entry.getResponder();
            while ((player = mPlayers.getRelative(-1, player)) != entry.getPlayer()) {
                c.gridx--;

                ImageComponent nonResponder = new ImageComponent(
                        ImageUtils.getColorConvertedImage(player.getCharacter().getTokenImage(), ColorSpace.CS_GRAY),
                        tokenSize
                );
                TextBubble.createToolTip(nonResponder, TextBubble.ABOVE, player.getName() + " had no cards");
                add(nonResponder, c);
            }
        }
    }
}

