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
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;
import com.wolfetones.cluedo.game.Location;

public abstract class Token extends Card {
    private TokenOccupiableTile mTile;
    private TokenTileListener mTileListener;
    private Location mLocation;

    Token(int id, String name) {
        super(id, name);
    }

    public void setLocation(Location location) {
        mLocation = location;

        if (location.isRoom()) {
            // Find an unoccupied room tile
            setTile(((Room) location).getNextUnoccupiedTile(this));
        } else {
            setTile((Tile) location);
        }
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setTile(Tile tile) {
        if (!(tile instanceof TokenOccupiableTile)) {
            throw new IllegalStateException("Tile " + tile + " is not of class TokenOccupiableTile");
        }

        // Remove token from old tile
        if (mTile != null) {
            mTile.setToken(null);
        }

        mTile = (TokenOccupiableTile) tile;
        mTile.setToken(this);

        // Notify the tile listener
        if (mTileListener != null) {
            mTileListener.onTileSet(tile);
        }
    }

    public TokenOccupiableTile getTile() {
        return mTile;
    }

    public void setTileListener(TokenTileListener listener) {
        mTileListener = listener;
    }

    public interface TokenTileListener {
        void onTileSet(Tile tile);
    }
}
