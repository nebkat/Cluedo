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

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class CardPickerCardComponent<T extends Card> extends JComponent implements MouseListener {
    private CardPickerPanel<T> mPanel;
    private T mCard;
    private Font mFont;

    private BufferedImage mImage;

    private int mImageWidth;
    private int mImageHeight;

    private boolean mMouseOver = false;
    private boolean mSelected = false;

    public CardPickerCardComponent(CardPickerPanel<T> panel, T card) {
        mPanel = panel;
        mCard = card;

        mFont = Config.FONT.deriveFont(Font.PLAIN, Config.screenRelativeSize(16));

        mImage = mCard.getCardImage();

        mImageWidth = Config.screenRelativeSize(100);
        mImageHeight = mImage.getHeight() * mImageWidth / mImage.getWidth();

        setPreferredSize(new Dimension(mImageWidth, mImageHeight + Config.screenRelativeSize(20)));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(this);
    }

    public T getCard() {
        return mCard;
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;

        // Set alpha
        float alpha = 0.5f;
        if (mMouseOver) {
            alpha = 1.0f;
        } else if (mSelected) {
            alpha = 0.9f;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Draw base card
        g.drawImage(mImage, 0, 0, mImageWidth, mImageHeight,
                0, 0, mImage.getWidth(), mImage.getHeight(), null);

        // Draw selected/highlighted overlays
        if (mSelected) {
            g.drawImage(Card.getCardSelectedOverlayImage(), 0, 0, mImageWidth, mImageHeight, null);
        } else if (mMouseOver) {
            g.drawImage(Card.getCardHighlightOverlayImage(), 0, 0, mImageWidth, mImageHeight, null);
        }

        // Draw card name
        g.setFont(mFont);
        Util.drawCenteredString(mCard.getName(), 0, getHeight() - mFont.getSize(), getWidth(), mFont.getSize(), g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        setSelected(true);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setMouseOver(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setMouseOver(false);
    }

    public void setMouseOver(boolean mouseOver) {
        mMouseOver = mouseOver;
        repaint();
    }

    public void setSelected(boolean selected) {
        if (selected) {
            mPanel.onCardComponentSelected(this);
        }

        mSelected = selected;
        repaint();
    }
}
