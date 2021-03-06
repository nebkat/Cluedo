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

import com.wolfetones.cluedo.GameController;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.game.Game;
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.ui.component.ImageComponent;
import com.wolfetones.cluedo.ui.component.TextBubble;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActionPanel extends JPanel {
    private static final int BUTTON_ROLL = 0;
    private static final int BUTTON_PASSAGE = BUTTON_ROLL + 1;
    private static final int BUTTON_SUGGEST = BUTTON_PASSAGE + 1;
    private static final int BUTTON_ACCUSE = BUTTON_SUGGEST + 1;
    private static final int BUTTON_STOP = BUTTON_ACCUSE + 1;
    private static final int BUTTON_DONE = BUTTON_STOP + 1;

    private static final ButtonDescription[] BUTTON_DESCRIPTIONS = {
            new ButtonDescription("roll", "Roll dice", GameController.COMMAND_ROLL),
            new ButtonDescription("passage", "Use secret passage", GameController.COMMAND_PASSAGE),
            new ButtonDescription("suggest", "Pose question", GameController.COMMAND_QUESTION),
            new ButtonDescription("accuse", "Make final accusation", GameController.COMMAND_ACCUSE),
            new ButtonDescription("stop", "Stop moving", GameController.COMMAND_STOP),
            new ButtonDescription("done", "Finish turn", GameController.COMMAND_DONE)
    };

    private static class ButtonDescription {
        private String icon;
        private String text;
        private String command;

        private ButtonDescription(String i, String t, String c) {
            icon = i;
            text = t;
            command = c;
        }
    }

    private List<ActionButton> mButtons = new ArrayList<>();

    @SuppressWarnings("SuspiciousNameCombination")
    public ActionPanel(Consumer<String> actionCommandListener, int iconSize) {
        super();

        setOpaque(false);
        setLayout(new GridBagLayout());

        // Push everything to bottom and ensure text bubble column width
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        add(Box.createGlue(), c);

        // Add buttons
        c = new GridBagConstraints();
        c.gridy = 0;
        for (ButtonDescription description : BUTTON_DESCRIPTIONS) {
            ActionButton button = new ActionButton(ImageUtils.loadImage("icons/" + description.icon + ".png"), iconSize, iconSize);
            button.clickAction(() -> actionCommandListener.accept(description.command));

            mButtons.add(button);

            c.gridy++;
            add(button, c);

            button.setTextBubble(TextBubble.createToolTip(button, TextBubble.RIGHT, description.text));
        }
    }

    public void updateStatus(Game game) {
        mButtons.get(BUTTON_ROLL).setVisible(game.canRollDice());
        mButtons.get(BUTTON_PASSAGE).setVisible(game.canUsePassage());
        mButtons.get(BUTTON_SUGGEST).setVisible(game.canPoseQuestion());
        mButtons.get(BUTTON_ACCUSE).setVisible(game.canMakeFinalAccusation());
        mButtons.get(BUTTON_STOP).setVisible(game.canStopMoving());
        mButtons.get(BUTTON_DONE).setVisible(true);
        mButtons.get(BUTTON_DONE).setActive(game.isTurnFinished());
    }

    public void hideAllExceptDone() {
        mButtons.forEach(b -> b.setVisible(b == mButtons.get(BUTTON_DONE)));
    }

    private static class ActionButton extends ImageComponent implements Animator.Scalable {
        private boolean mEnabled = true;

        private double mScale = 0.75;

        private TextBubble mTextBubble;

        private ActionButton(BufferedImage image, int width, int height) {
            super(image, width, height);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setMouseOver(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setMouseOver(false);
                }
            });

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void setTextBubble(TextBubble bubble) {
            mTextBubble = bubble;
        }

        @Override
        public void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;
            Util.setHighQualityRenderingHints(g);

            if (mScale == 0) {
                return;
            } else if (mScale != 1) {
                g.translate(getWidth() / 2, getHeight() / 2);
                g.scale(mScale, mScale);
                g.translate(-getWidth() / 2, -getHeight() / 2);
            }

            if (!mEnabled) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            }

            g.drawImage(mImage, 0, 0, null);
        }

        private void setActive(boolean enabled) {
            mEnabled = enabled;
            setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

            repaint();
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);

            if (mTextBubble != null) {
                mTextBubble.resetBubble();
            }
        }

        private void clickAction(Runnable runnable) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (mEnabled) {
                        runnable.run();
                    }
                }
            });
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

        private void setMouseOver(boolean mouseOver) {
            Animator.getInstance().animateAndInterruptAll(this)
                    .scale(mouseOver ? 1.0 : 0.75)
                    .setDuration(200)
                    .start();
        }
    }
}
