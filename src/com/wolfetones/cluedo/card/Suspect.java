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

    public Suspect(String name, String[] searchNames, String cardImage, Color color) {
        super(name, searchNames, cardImage);

        mColor = color;
    }

    /**
     * Gets the suspect's associated color.
     *
     * @return the color of the suspect
     */
    public Color getColor() {
        return mColor;
    }

    /**
     * Sets whether the player has been moved to a different room since their last turn.
     *
     * @see Suspect#getMovedSinceLastTurn()
     *
     * @param moved whether the player has been moved since their last turn
     */
    public void setMovedSinceLastTurn(boolean moved) {
        mMovedSinceLastTurn = moved;
    }

    /**
     * Gets whether the player has been moved to a different room since their last turn.
     *
     * If the player has been moved, they are allowed to pose questions in their current room without moving.
     *
     * @return whether the player has been moved since their last turn
     */
    public boolean getMovedSinceLastTurn() {
        return mMovedSinceLastTurn;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected String getCardImageSuffix() {
        return "suspect";
    }
}
