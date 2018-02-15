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

import com.wolfetones.cluedo.SwingTranslateAnimator;
import com.wolfetones.cluedo.card.Token;

import javax.swing.*;

public abstract class TokenComponent extends JComponent {
    private int mTileSize;
    protected Token mToken;

    private SwingTranslateAnimator mAnimator = new SwingTranslateAnimator(this, 200);

    protected TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
        updateCoordinatesImmediately();

        token.setCoordinatesListener(this::updateCoordinates);
    }

    public Token getToken() {
        return mToken;
    }

    private void updateCoordinates() {
        mAnimator.translate((int) (mToken.getCoordinateX() * mTileSize),
                (int) (mToken.getCoordinateY() * mTileSize));
        mAnimator.start();
    }

    private void updateCoordinatesImmediately() {
        setLocation((int) (mToken.getCoordinateX() * mTileSize), (int) (mToken.getCoordinateY() * mTileSize));
    }
}
