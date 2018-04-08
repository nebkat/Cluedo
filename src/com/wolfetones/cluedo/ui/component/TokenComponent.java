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

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.card.Token;
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class TokenComponent extends JComponent {
    public static final int ANIMATION_DURATION = 200;

    private int mTileSize;
    private Token mToken;

    private BufferedImage mTokenImage;

    public TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
        updateCoordinatesImmediately();

        token.setCoordinatesListener(this::updateCoordinates);

        mTokenImage = ImageUtils.getScaledImage(mToken.getTokenImage(), getWidth(), getHeight());

        TextBubble.createToolTip(this, TextBubble.ABOVE, token.getName());
    }

    @Override
    public void paintComponent(Graphics g) {
        Util.setHighQualityRenderingHints(g);

        g.drawImage(mTokenImage, 0, 0, null);
    }

    private void updateCoordinates(List<? extends Tile> path, int delay) {
        int finalX = (int) (mToken.getCoordinateX() * mTileSize);
        int finalY = (int) (mToken.getCoordinateY() * mTileSize);

        Animator.Animation animation = Animator.getInstance().animateAndInterruptAll(this);
        if (path != null) {
            for (Tile tile : path) {
                int targetX = tile.getX() * mTileSize;
                int targetY = tile.getY() * mTileSize;

                animation.translate(targetX, targetY)
                        .setDuration(ANIMATION_DURATION);

                animation = animation.chain();
            }
        }

        animation.translate(finalX, finalY)
                .setDuration(200)
                .setDelay(delay * ANIMATION_DURATION)
                .start();
    }

    private void updateCoordinatesImmediately() {
        setLocation((int) (mToken.getCoordinateX() * mTileSize), (int) (mToken.getCoordinateY() * mTileSize));
    }
}
