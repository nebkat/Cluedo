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

package com.wolfetones.cluedo.ui.component;

import com.wolfetones.cluedo.util.Util;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.ui.Animator;

import javax.swing.*;
import java.awt.*;

public class TextBubble extends JComponent implements Animator.Scalable {
    private static final float FONT_PERCENTAGE = 0.30f;
    private static final float BACKGROUND_PADDING_PERCENTAGE = 0.20f;
    private static final float POINTER_WIDTH_PERCENTAGE = 0.15f;

    private JLabel mLabel;
    private JButton mButton;
    private Runnable mButtonAction;

    private double mScale = 0.0;

    public TextBubble(int height) {
        super();

        setLayout(new FlowLayout(FlowLayout.LEFT, Config.screenRelativeSize(5), 0));

        int fontHeight = (int) (height * FONT_PERCENTAGE);

        mLabel = new JLabel();
        mButton = new JButton();

        mLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontHeight));
        mButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontHeight));

        mLabel.setText(" ");
        mButton.setText(" ");

        mButton.setMargin(new Insets(Config.screenRelativeSize(-2),
                Config.screenRelativeSize(7),
                Config.screenRelativeSize(-2),
                Config.screenRelativeSize(7)));
        mButton.setVisible(false);
        mButton.addActionListener(e -> mButtonAction.run());

        add(mLabel);
        add(mButton);

        int textHeight = mLabel.getPreferredSize().height;
        int textMargin = (height - textHeight) / 2;
        int pointerWidth = (int) (height * POINTER_WIDTH_PERCENTAGE);

        setBorder(BorderFactory.createEmptyBorder(
                textMargin,
                pointerWidth + textMargin,
                textMargin,
                textMargin
        ));

        setVisible(false);

        setBackground(new Color(255, 255, 255, 200));

        updateSize();
    }

    public void setText(String text) {
        mLabel.setText(text);

        updateSize();
    }

    public void setButton(String text, Runnable action) {
        if (text == null) {
            mButton.setVisible(false);
            return;
        } else {
            mButton.setVisible(true);
        }

        mButton.setText(text);
        mButtonAction = action;

        updateSize();
    }

    private void updateSize() {
        setSize(getPreferredSize());
    }

    public void showBubble() {
        showBubble(0);
    }

    public void showBubble(int delay) {
        Animator.getInstance().animateAndInterruptAll(this)
                .setDuration(250)
                .scale(1.0)
                .setDelay(delay)
                .before(() -> setVisible(true))
                .start();
    }

    public void hideBubble() {
        Animator.getInstance().animateAndInterruptAll(this)
                .setDuration(250)
                .scale(0.0)
                .after(() -> {
                    setVisible(false);
                    setButton(null, null);
                })
                .start();
    }

    public void resetBubble() {
        Animator.getInstance().interruptAllAnimations(this);
        setVisible(false);
        setButton(null, null);
        mScale = 0.0;
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        Util.setHighQualityRenderingHints(g);

        if (mScale == 0) {
            g.translate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
        } else if (mScale != 1) {
            g.translate(0, getHeight() / 2);
            g.scale(mScale, mScale);
            g.translate(0, -getHeight() / 2);
        }

        int backgroundPadding = (int) (getHeight() * BACKGROUND_PADDING_PERCENTAGE);
        int pointerWidth = (int) (getHeight() * POINTER_WIDTH_PERCENTAGE);

        g.setColor(getBackground());

        // Draw rounded rectangle
        g.fillRoundRect(pointerWidth,
                backgroundPadding,
                getWidth() - pointerWidth,
                getHeight() - 2 * backgroundPadding,
                getHeight() / 3, getHeight() / 3);

        // Draw pointer
        g.fillPolygon(new int[]{0, pointerWidth, pointerWidth},
                new int[]{getHeight() / 2, getHeight() / 2 - pointerWidth / 2, getHeight() / 2 + pointerWidth / 2},
                3);
    }

    @Override
    public double getScale() {
        return mScale;
    }

    @Override
    public void setScale(double scale) {
        mScale = scale;

        repaint();
    }
}