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
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.ui.component.ScaledImageComponent;
import com.wolfetones.cluedo.ui.component.TileBorder;
import com.wolfetones.cluedo.ui.component.TileComponent;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

public class SlideOutCardsPanel extends JPanel {
    private String mTitle;
    private Font mFont;

    private int mHiddenWidth;
    private int mContainerWidth;

    public SlideOutCardsPanel(String title, int hiddenWidth, int height, int containerWidth) {
        super();

        setLayout(new GridBagLayout());
        setOpaque(false);

        mTitle = title;
        mFont = new Font(Font.SANS_SERIF, Font.BOLD, hiddenWidth / 2);
        mHiddenWidth = hiddenWidth;
        mContainerWidth = containerWidth;

        setBorder(BorderFactory.createEmptyBorder(Config.screenRelativeSize(10), hiddenWidth, Config.screenRelativeSize(10), 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                slideIn();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                slideOut();
            }
        });

        setSize(mHiddenWidth, height);
        setMinimumSize(new Dimension(mHiddenWidth, height));
    }

    private void slideIn() {
        Animator.getInstance().animateAndInterruptAll(this)
                .translate(mContainerWidth - getWidth(), getY())
                .setDuration(500)
                .start();
    }

    private void slideOut() {
        Animator.getInstance().animateAndInterruptAll(this)
                .translate(mContainerWidth - mHiddenWidth, getY())
                .setDuration(500)
                .start();
    }

    public void setCards(List<? extends Card> cards) {
        removeAll();

        GridBagConstraints imageConstraints = new GridBagConstraints();
        imageConstraints.gridy = 0;
        imageConstraints.ipadx = Config.screenRelativeSize(5);
        imageConstraints.ipady = Config.screenRelativeSize(5);

        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridy = 1;

        int fontSize = 16;
        int cardHeight = getHeight() * 7 / 10;
        if (cards.size() > 6) {
            cardHeight = getHeight() / 2;
            fontSize = 10;
        }

        int cardWidth = cardHeight * Card.getCardBackImage().getWidth() / Card.getCardBackImage().getHeight();

        for (Card card : cards) {
            ScaledImageComponent image = new ScaledImageComponent(card.getCardImage(), cardWidth);

            JLabel label = new JLabel(card.getName());
            label.setForeground(Color.BLACK);
            label.setFont(Config.FONT.deriveFont(Font.PLAIN, Config.screenRelativeSize(fontSize)));

            add(image, imageConstraints);
            add(label, textConstraints);
        }

        setSize(getPreferredSize().width, getHeight());

        slideOut();

        revalidate();
        repaint();
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        Util.setHighQualityRenderingHints(g);

        int arcSize = Config.screenRelativeSize(10);
        g.setColor(TileBorder.COLOR_WALL);
        g.fillRoundRect(0, 0, getWidth() + arcSize, getHeight(), arcSize, arcSize);

        int borderSize = Config.screenRelativeSize(5);
        g.setColor(TileComponent.COLOR_ROOM);
        g.fillRoundRect(borderSize, borderSize, getWidth() + arcSize, getHeight() - 2 * borderSize, arcSize, arcSize);

        // Save transform
        AffineTransform transform = g.getTransform();

        // Rotate for vertical text
        g.translate(0, getHeight());
        g.rotate(-Math.PI / 2);

        g.setColor(Color.BLACK);
        g.setFont(mFont);
        Util.drawCenteredString(mTitle, 0, borderSize, getHeight(), mHiddenWidth - borderSize, g);

        // Reset transform
        g.setTransform(transform);
    }
}
