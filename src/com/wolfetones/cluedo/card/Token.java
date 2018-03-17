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

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.board.Location;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class Token extends Card {
    private Location mLocation;

    private Runnable mCoordinatesUpdateListener;
    private float mCoordinateX;
    private float mCoordinateY;

    private BufferedImage mTokenImage;

    Token(int id, String name, String resourceName) {
        super(id, name, resourceName);

        if (resourceName != null) {
            String imageFile = "token-" + getCardImageSuffix() + "-" + resourceName + ".png";
            mTokenImage = Util.loadImage(imageFile);
        }
    }

    /**
     * Sets the {@link Location} of the {@code Token}.
     *
     * If the {@code Location} is a {@code Room}, the token is added to the room.
     * If the {@code Location} is a {@code CorridorTile}, the tile's token is set.
     *
     * @param location The {@code Location} of the {@code Token}.
     */
    public void setLocation(Location location) {
        if (mLocation != null) {
            if (mLocation.isRoom()) {
                mLocation.asRoom().removeToken(this);
            } else {
                mLocation.asTile().setToken(null);
            }
        }

        mLocation = location;

        if (location.isRoom()) {
            location.asRoom().addToken(this);

            // Coordinates will be handled by room
        } else {
            location.asTile().setToken(this);

            setCoordinates(location.asTile().getX(), location.asTile().getY());
        }
    }

    /**
     * Gets the {@link Location} of the {@code Token}.
     *
     * @return The {@code Location} of the {@code Token}.
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Sets the board coordinates of the {@code Token}.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public void setCoordinates(float x, float y) {
        mCoordinateX = x;
        mCoordinateY = y;

        if (mCoordinatesUpdateListener != null) {
            mCoordinatesUpdateListener.run();
        }
    }

    public float getCoordinateX() {
        return mCoordinateX;
    }

    public float getCoordinateY() {
        return mCoordinateY;
    }

    /**
     * Sets the coordinates update listener.
     *
     * Allows the token to notify its respective {@link com.wolfetones.cluedo.ui.TokenComponent} of coordinate changes.
     *
     * @param listener Runnable to run.
     */
    public void setCoordinatesListener(Runnable listener) {
        mCoordinatesUpdateListener = listener;
    }

    public BufferedImage getTokenImage() {
        return mTokenImage;
    }
}
