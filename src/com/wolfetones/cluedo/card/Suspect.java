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

package com.wolfetones.cluedo.card;

import java.awt.Color;

/**
 * Suspect
 */
public class Suspect extends Token {
    private Color mColor;
    private boolean mMovedSinceLastTurn = false;

    public Suspect(int id, String name, String cardImage, Color color) {
        super(id, name, cardImage);

        mColor = color;
    }

    public Color getColor() {
        return mColor;
    }

    public void setMovedSinceLastTurn(boolean moved) {
        mMovedSinceLastTurn = moved;
    }

    public boolean getMovedSinceLastTurn() {
        return mMovedSinceLastTurn;
    }

    @Override
    protected String getCardImageSuffix() {
        return "suspect";
    }
}
