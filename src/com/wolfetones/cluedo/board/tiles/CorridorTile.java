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

import com.wolfetones.cluedo.game.Location;

/**
 * Corridor tile
 */
public class CorridorTile extends TokenOccupiableTile implements Location {
    public CorridorTile(int x, int y) {
        super(x, y);
    }

    @Override
    public void setNeighbours(Tile left, Tile up, Tile right, Tile down) {
        super.setNeighbours(left, up, right, down);

        if (left instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) left);
        if (up instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) up);
        if (right instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) right);
        if (down instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) down);
    }

    /**
     * Returns {@code true} if a {@code Token} can move into any neighbouring tile.
     *
     * @return {@code true} if a {@code Token} can move into any neighbouring tile.
     * @see #canMoveToTile
     */
    public boolean canMove() {
        return canMoveLeft() || canMoveUp() || canMoveRight() || canMoveDown();
    }

    /**
     * Returns {@code true} if a {@code Token} can move into the tile to the left of this tile.
     *
     * @return {@code true} if a {@code Token} can move into the tile to the left of this tile.
     * @see #canMoveToTile
     */
    public boolean canMoveLeft() {
        return canMoveToTile(mLeft);
    }

    /**
     * Returns {@code true} if a {@code Token} can move into the tile above this tile.
     *
     * @return {@code true} if a {@code Token} can move into the tile above this tile.
     * @see #canMoveToTile
     */
    public boolean canMoveUp() {
        return canMoveToTile(mUp);
    }

    /**
     * Returns {@code true} if a {@code Token} can move into the tile to the right of this tile.
     *
     * @return {@code true} if a {@code Token} can move into the tile to the right of this tile.
     * @see #canMoveToTile
     */
    public boolean canMoveRight() {
        return canMoveToTile(mRight);
    }

    /**
     * Returns {@code true} if a {@code Token} can move into the tile below this tile.
     *
     * @return {@code true} if a {@code Token} can move into the tile below this tile.
     * @see #canMoveToTile
     */
    public boolean canMoveDown() {
        return canMoveToTile(mDown);
    }

    /**
     * Return {@code true} if a {@code Token} can move into the specified tile.
     *
     * If the tile is a {@code CorridorTile} it checks whether it is empty.
     *
     * @param t the tile into which possible movement is being questioned.
     * @return {@code true} if a {@code Token} can move into the specified tile.
     */
    private boolean canMoveToTile(Tile t) {
        return (t instanceof CorridorTile && ((CorridorTile) t).isFree()) ||
                (t instanceof RoomTile && mTokenTraversableNeighbours.contains(t));
    }

    public void addDoor(RoomTile t) {
        mTokenTraversableNeighbours.add(t);
    }
}
