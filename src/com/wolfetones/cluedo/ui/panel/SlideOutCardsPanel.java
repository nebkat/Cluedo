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
import com.wolfetones.cluedo.ui.component.ScaledImageComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SlideOutCardsPanel extends SlideOutPanel {

    public SlideOutCardsPanel(int location, String title, int handleSize, int handleWidth, int containerSize) {
        super(location, title, handleSize, handleWidth, containerSize, true);

        setLayout(new GridBagLayout());
    }

    public void setCards(List<? extends Card> cards) {
        removeAll();

        Collections.sort(cards);

        GridBagConstraints imageConstraints = new GridBagConstraints();
        if (!isVertical()) {
            imageConstraints.gridy = 0;
        } else {
            imageConstraints.gridx = 0;
        }
        imageConstraints.ipadx = Config.screenRelativeSize(5);
        imageConstraints.ipady = Config.screenRelativeSize(5);

        GridBagConstraints textConstraints = new GridBagConstraints();
        if (!isVertical()) {
            textConstraints.gridy = 1;
        } else {
            textConstraints.gridx = 0;
        }

        int fontSize = 16;
        int cardWidth;

        if (!isVertical()) {
            int cardHeight = getHeight() * 7 / 10;
            if (cards.size() > 6) {
                cardHeight = getHeight() * 6 / 10;
                fontSize = 12;
            }

            cardWidth = cardHeight * Card.getCardBackImage().getWidth() / Card.getCardBackImage().getHeight();
        } else {
            cardWidth = getWidth() * 3 / 5;
        }

        for (Card card : cards) {
            ScaledImageComponent image = new ScaledImageComponent(card.getCardImage(), cardWidth);

            JLabel label = new JLabel(card.getName());
            label.setForeground(Color.BLACK);
            label.setFont(Config.FONT.deriveFont(Font.PLAIN, Config.screenRelativeSize(fontSize)));

            add(image, imageConstraints);
            add(label, textConstraints);
        }

        reposition();
    }
}
