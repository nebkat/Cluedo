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
import com.wolfetones.cluedo.card.Token;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class TokenComponent extends JComponent {
    private int mTileSize;
    protected Token mToken;

    private SwingTranslateAnimator mTranslateAnimator = new SwingTranslateAnimator(this, 200);

    private BufferedImage mTokenImage;

    protected TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
        updateCoordinatesImmediately();

        token.setCoordinatesListener(this::updateCoordinates);

        mTokenImage = Util.getScaledImage(mToken.getTokenImage(), getWidth(), getHeight());
    }

    public Token getToken() {
        return mToken;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(mTokenImage, 0, 0, null);
    }

    private void updateCoordinates() {
        mTranslateAnimator.translate((int) (mToken.getCoordinateX() * mTileSize),
                (int) (mToken.getCoordinateY() * mTileSize));
        mTranslateAnimator.start();
    }

    private void updateCoordinatesImmediately() {
        setLocation((int) (mToken.getCoordinateX() * mTileSize), (int) (mToken.getCoordinateY() * mTileSize));
    }
}
