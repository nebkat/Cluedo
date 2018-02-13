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

import com.wolfetones.cluedo.card.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Tile that can hold a {@link Token}.
 */
public abstract class TokenOccupiableTile extends Tile {
    protected Token mToken;

    protected List<TokenOccupiableTile> mTokenTraversableNeighbours = new ArrayList<>();

    TokenOccupiableTile(int x, int y) {
        super(x, y);
    }

    /**
     * Sets the {@code Token} that is currently occupying this tile.
     *
     * @param token the {@code Token} that is currently occupying this tile.
     */
    public void setToken(Token token) {
        mToken = token;
    }

    /**
     * Gets the {@code Token} that is currently occupying this tile.
     *
     * @return the {@code Token} that is currently occupying this tile.
     */
    public Token getToken() {
        return mToken;
    }

    /**
     * Provides a list of all neighbouring tiles that can be reached by a {@code Token} from this tile.
     *
     * @return A list of
     */
    public List<TokenOccupiableTile> getTokenTraversableNeighbours() {
        return mTokenTraversableNeighbours;
    }

    /**
     * Returns {@code true} if this tile is occupied by a {@code Token}
     *
     * @return {@code true} if this tile is occupied by a {@code Token}
     */
    public boolean isOccupied() {
        return mToken != null;
    }

    /**
     * Returns {@code true} if this tile is not occupied by a {@code Token}
     *
     * @return {@code true} if this tile is not occupied by a {@code Token}
     */
    public boolean isFree() {
        return mToken == null;
    }
}
