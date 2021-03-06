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

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.board.Location;
import com.wolfetones.cluedo.ui.component.TokenComponent;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base token class, representing cards that also have a token on the board.
 */
public abstract class Token extends Card {
    private Location mLocation;

    private CoordinatesUpdateListener mCoordinatesUpdateListener;
    private float mCoordinateX;
    private float mCoordinateY;

    private String mTokenImage;

    Token(String name, String[] searchNames, String resourceName) {
        super(name, searchNames, resourceName);

        if (resourceName != null) {
            mTokenImage = "tokens/token-" + getCardImageSuffix() + "-" + resourceName + ".png";
        }
    }

    /**
     * Sets the {@link Location} of the {@code Token}.
     *
     * If the {@code Location} is a {@code Room}, the token is added to the room.
     * If the {@code Location} is a {@code CorridorTile}, the tile's token is set.
     *
     * @param location the {@code Location} of the {@code Token}
     */
    public void setLocation(Location location, List<? extends Tile> path) {
        if (mLocation != null) {
            if (mLocation.isRoom()) {
                mLocation.asRoom().removeToken(this);
            } else {
                mLocation.asTile().setToken(null);
            }
        }

        mLocation = location;

        if (location.isRoom()) {
            location.asRoom().addToken(this, path);

            // Coordinates will be handled by room
        } else {
            location.asTile().setToken(this);

            setCoordinates(location.asTile().getX(), location.asTile().getY(), path, 0);
        }
    }

    /**
     * Gets the {@link Location} of the {@code Token}.
     *
     * @return the {@code Location} of the {@code Token}
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Sets the board coordinates of the {@code Token}.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setCoordinates(float x, float y, List<? extends Tile> path, int delay) {
        mCoordinateX = x;
        mCoordinateY = y;

        if (mCoordinatesUpdateListener != null) {
            mCoordinatesUpdateListener.update(path, delay);
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
     * Allows the token to notify its respective {@link TokenComponent} of coordinate changes, and the path taken
     * to those coordinates.
     *
     * @param listener the coordinate update listener
     */
    public void setCoordinatesListener(CoordinatesUpdateListener listener) {
        mCoordinatesUpdateListener = listener;
    }

    /**
     * Loads and returns the token image associated with this card.
     *
     * @return the token image associated with this card
     */
    public BufferedImage getTokenImage() {
        return ImageUtils.loadImage(mTokenImage);
    }

    public interface CoordinatesUpdateListener {
        void update(List<? extends Tile> path, int delay);
    }
}
