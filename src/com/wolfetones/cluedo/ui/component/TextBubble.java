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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TextBubble extends JComponent implements Animator.Scalable {
    private static final float FONT_PERCENTAGE = 0.45f;
    private static final float POINTER_SIZE_PERCENTAGE = 0.2f;
    private static final int DEFAULT_HEIGHT = Config.screenRelativeSize(40);

    public static final int LEFT = 0;
    public static final int ABOVE = 1;
    public static final int RIGHT = 2;
    public static final int BELOW = 3;

    private int mDirection;
    private int mPointerSize;

    private JLabel mLabel;
    private JButton mButton;
    private Runnable mButtonAction;

    private double mScale = 0.0;

    public TextBubble(int direction) {
        super();

        mDirection = direction;
        mPointerSize = (int) (DEFAULT_HEIGHT * POINTER_SIZE_PERCENTAGE);

        setLayout(new FlowLayout(FlowLayout.LEFT, Config.screenRelativeSize(5), 0));

        int fontHeight = (int) (DEFAULT_HEIGHT * FONT_PERCENTAGE);

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
        int textMargin = (DEFAULT_HEIGHT - textHeight) / 2;
        int pointerWidth = (int) (DEFAULT_HEIGHT * POINTER_SIZE_PERCENTAGE);

        setBorder(BorderFactory.createEmptyBorder(
                textMargin + (direction == BELOW ? pointerWidth : 0),
                textMargin + (direction == RIGHT ? pointerWidth : 0),
                textMargin + (direction == ABOVE ? pointerWidth : 0),
                textMargin + (direction == LEFT ? pointerWidth : 0)
        ));

        setVisible(false);

        setBackground(new Color(255, 255, 255, 200));

        updateSize();
    }

    public static TextBubble createToolTip(JComponent component, int direction, String text) {
        TextBubble bubble = new TextBubble(direction);
        bubble.setText(text);
        JWindow window = new JWindow();
        window.add(bubble);
        window.setBackground(new Color(0, true));
        window.setSize(bubble.getPreferredSize());

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Point location = component.getLocationOnScreen();

                if (direction == ABOVE || direction == BELOW) {
                    location.x += component.getWidth() / 2 - bubble.getWidth() / 2;
                } else if (direction == LEFT || direction == RIGHT) {
                    location.y += component.getHeight() / 2 - bubble.getHeight() / 2;
                }

                if (direction == ABOVE) {
                    location.y -= bubble.getHeight();
                } else if (direction == LEFT) {
                    location.x -= bubble.getWidth();
                } else if (direction == BELOW) {
                    location.y += component.getHeight();
                } else if (direction == RIGHT) {
                    location.x += component.getWidth();
                }

                window.setLocation(location);
                window.setVisible(true);
                bubble.showBubble();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bubble.hideBubble(() -> window.setVisible(false));
            }
        });

        return bubble;
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
        hideBubble(null);
    }

    public void hideBubble(Runnable runnable) {
        Animator.getInstance().animateAndInterruptAll(this)
                .setDuration(250)
                .scale(0.0)
                .after(() -> {
                    setVisible(false);
                    setButton(null, null);
                    if (runnable != null) {
                        runnable.run();
                    }
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
            int pointerX = 0;
            int pointerY = 0;

            if (mDirection == ABOVE || mDirection == BELOW) {
                pointerX = getWidth() / 2;
            } else if (mDirection == LEFT || mDirection == RIGHT) {
                pointerY = getHeight() / 2;
            }

            if (mDirection == LEFT) {
                pointerX = getWidth();
            } else if (mDirection == ABOVE) {
                pointerY = getHeight();
            }


            g.translate(pointerX, pointerY);
            g.scale(mScale, mScale);
            g.translate(-pointerX, -pointerY);
        }

        g.setColor(getBackground());

        // Draw background rectangle
        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();
        if (mDirection == ABOVE || mDirection == BELOW) {
            height -= mPointerSize;
        } else if (mDirection == LEFT || mDirection == RIGHT) {
            width -= mPointerSize;
        }
        if (mDirection == RIGHT) {
            x = mPointerSize;
        } else if (mDirection == BELOW) {
            y = mPointerSize;
        }
        g.fillRoundRect(x, y, width, height, mPointerSize * 2, mPointerSize * 2);

        // Draw pointer
        Polygon polygon = new Polygon();
        if (mDirection == ABOVE) {
            polygon.addPoint(getWidth() / 2, getHeight());
            polygon.addPoint(getWidth() / 2 - mPointerSize / 2, getHeight() - mPointerSize);
            polygon.addPoint(getWidth() / 2 + mPointerSize / 2, getHeight() - mPointerSize);
        } else if (mDirection == LEFT) {
            polygon.addPoint(getWidth(), getHeight() / 2);
            polygon.addPoint(getWidth() - mPointerSize, getHeight() / 2 - mPointerSize / 2);
            polygon.addPoint(getWidth() - mPointerSize, getHeight() / 2 + mPointerSize / 2);
        } else if (mDirection == BELOW) {
            polygon.addPoint(getWidth() / 2, 0);
            polygon.addPoint(getWidth() / 2 - mPointerSize / 2, mPointerSize);
            polygon.addPoint(getWidth() / 2 + mPointerSize / 2, mPointerSize);
        } else if (mDirection == RIGHT) {
            polygon.addPoint(0, getHeight() / 2);
            polygon.addPoint(mPointerSize, getHeight() / 2 - mPointerSize / 2);
            polygon.addPoint(mPointerSize, getHeight() / 2 + mPointerSize / 2);
        }
        g.fillPolygon(polygon);
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