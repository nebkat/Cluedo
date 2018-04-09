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

import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.ui.component.TileBorder;
import com.wolfetones.cluedo.ui.component.TileComponent;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class SlideOutPanel extends JPanel {
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    private String mTitle;
    private Font mFont;

    private boolean mVertical;
    private int mLocation;
    private int mHandleSize;
    private int mContainerSize;
    private int mContainerLocation;

    private boolean mShown = false;

    @SuppressWarnings("SuspiciousNameCombination")
    public SlideOutPanel(int location, String title, int handleSize, int handleWidth, int containerSize, int containerLocation, boolean hover) {
        super();

        int handlePadding = Config.screenRelativeSize(10);
        Border border;
        if (location == LEFT) {
            border = BorderFactory.createEmptyBorder(handlePadding, 0, handlePadding, handleSize);
        } else if (location == TOP) {
            border = BorderFactory.createEmptyBorder(0, handlePadding, handleSize, handlePadding);
        } else if (location == RIGHT) {
            border = BorderFactory.createEmptyBorder(handlePadding, handleSize, handlePadding, 0);
        } else if (location == BOTTOM) {
            border = BorderFactory.createEmptyBorder(handleSize, handlePadding, 0, handlePadding);
        } else {
            throw new IllegalArgumentException("Invalid location, must be one of SlideOutPanel.LEFT, SlideOutPanel.TOP, SlideOutPanel.RIGHT, SlideOutPanel.BOTTOM");
        }
        setBorder(border);
        setOpaque(false);

        mLocation = location;
        mVertical = location == TOP || location == BOTTOM;

        mTitle = title;
        mFont = new Font(Font.SANS_SERIF, Font.BOLD, handleSize / 2);
        mHandleSize = handleSize;
        mContainerSize = containerSize;
        mContainerLocation = containerLocation;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!hover) {
                    toggle();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (hover) {
                    slideIn();
                }

                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Component c = SwingUtilities.getDeepestComponentAt(e.getComponent(), e.getX(), e.getY());
                if (c == null || !SwingUtilities.isDescendingFrom(c, SlideOutPanel.this)) {
                    if (hover) {
                        slideOut();
                    }
                }

                setCursor(Cursor.getDefaultCursor());
            }
        });

        if (mVertical) {
            setSize(handleWidth, handleSize);
        } else {
            setSize(handleSize, handleWidth);
        }
        reposition();
    }

    public boolean isVertical() {
        return mVertical;
    }

    public void toggle() {
        slide(!mShown);
    }

    public void slideIn() {
        slide(true);
    }

    public void slideOut() {
        slide(false);
    }

    private int getTargetX(boolean visible) {
        if (mVertical) {
            return mContainerLocation;
        } else {
            if (visible) {
                return mLocation == LEFT ? 0 : mContainerSize - getWidth();
            } else {
                return mLocation == LEFT ? mHandleSize - getWidth() : mContainerSize - mHandleSize;
            }
        }
    }

    private int getTargetY(boolean visible) {
        if (!mVertical) {
            return mContainerLocation;
        } else {
            if (visible) {
                return mLocation == TOP ? 0 : mContainerSize - getHeight();
            } else {
                return mLocation == TOP ? mHandleSize - getHeight() : mContainerSize - mHandleSize;
            }
        }
    }

    public void reposition() {
        setSize(mVertical ? getWidth() : getPreferredSize().width, !mVertical ? getHeight() : getPreferredSize().height);
        setLocation(getTargetX(false), getTargetY(false));
    }

    private void slide(boolean visible) {
        mShown = visible;

        Animator.getInstance().animateAndInterruptAll(this)
                .translate(getTargetX(visible), getTargetY(visible))
                .setDuration(500)
                .start();
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        Util.setHighQualityRenderingHints(g);

        int arcSize = Config.screenRelativeSize(10);
        int borderSize = Config.screenRelativeSize(5);

        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();

        // Fill outer border
        if (mLocation == LEFT) {
            x -= arcSize;
        } else if (mLocation == TOP) {
            y -= arcSize;
        }
        if (mVertical) {
            height += arcSize;
        } else {
            width += arcSize;
        }
        g.setColor(TileBorder.COLOR_WALL);
        g.fillRoundRect(x, y, width, height, arcSize, arcSize);

        // Fill inner color
        if (mLocation == RIGHT) {
            x += borderSize;
        } else if (mLocation == BOTTOM) {
            y += borderSize;
        }
        if (mVertical) {
            x += borderSize;
            width -= 2 * borderSize;
            height -= borderSize;
        } else {
            y += borderSize;
            width -= borderSize;
            height -= 2 * borderSize;
        }
        g.setColor(TileComponent.COLOR_ROOM);
        g.fillRoundRect(x, y, width, height, arcSize, arcSize);

        // Save transform
        AffineTransform transform = g.getTransform();

        // Rotate for vertical text
        if (mLocation == LEFT) {
            g.translate(getWidth(), 0);
            g.rotate(Math.toRadians(90));
        } else if (mLocation == TOP) {
            g.translate(0, getHeight() - mHandleSize - borderSize);
        } else if (mLocation == RIGHT) {
            g.translate(0, getHeight());
            g.rotate(Math.toRadians(-90));
        }

        g.setColor(Color.BLACK);
        g.setFont(mFont);
        Util.drawCenteredString(mTitle, borderSize, borderSize, mVertical ? width : height, mHandleSize - borderSize, g);

        // Reset transform
        g.setTransform(transform);
    }
}
