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

package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.ui.component.TileComponent;

/**
 * Base tile class, representing a tile on the board.
 */
public abstract class Tile {
    /**
     * Tile coordinates.
     */
    private int mX;
    private int mY;

    /**
     * Neighbouring tiles.
     */
    protected Tile mLeft;
    protected Tile mUp;
    protected Tile mRight;
    protected Tile mDown;

    private TileComponent mButton;

    /**
     * Base tile constructor.
     *
     * @param x X coordinate of this tile.
     * @param y Y coordinate of this tile.
     */
    Tile(int x, int y) {
        mX = x;
        mY = y;
    }

    /**
     * Returns the X coordinate of this tile.
     *
     * @return the X coordinate of this tile.
     */
    public int getX() {
        return mX;
    }

    /**
     * Returns the Y coordinate of this tile.
     *
     * @return the Y coordinate of this tile.
     */
    public int getY() {
        return mY;
    }

    /**
     * Returns the {@code Tile} to the left of this tile.
     *
     * @return the {@code Tile} to the left of this tile.
     */
    public Tile getLeft() {
        return mLeft;
    }

    /**
     * Returns the {@code Tile} above this tile.
     *
     * @return the {@code Tile} above this tile.
     */
    public Tile getUp() {
        return mUp;
    }

    /**
     * Returns the {@code Tile} to the right of this tile.
     *
     * @return the {@code Tile} to the right of this tile.
     */
    public Tile getRight() {
        return mRight;
    }

    /**
     * Returns the {@code Tile} below this tile.
     *
     * @return the {@code Tile} below this tile.
     */
    public Tile getDown() {
        return mDown;
    }

    /**
     * Sets the neighbouring {@code Tile}s.
     * @param left the {@code Tile} to the left of this tile.
     * @param up the {@code Tile} above this tile.
     * @param right the {@code Tile} to the right of this tile.
     * @param down the {@code Tile} below this tile.
     */
    public void setNeighbours(Tile left, Tile up, Tile right, Tile down) {
        mLeft = left;
        mUp = up;
        mRight = right;
        mDown = down;
    }

    public TileComponent getButton() {
        return mButton;
    }

    public void setButton(TileComponent button) {
        mButton = button;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + mX + ", " + mY + "]";
    }
}
