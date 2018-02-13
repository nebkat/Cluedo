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

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.card.Token;

import javax.swing.*;
import java.awt.*;

public abstract class TokenComponent extends JComponent implements Token.TokenTileListener {
    private int mTileSize;
    protected Token mToken;

    protected TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
        setTile(token.getTile());

        token.setTileListener(this);
    }

    public Token getToken() {
        return mToken;
    }

    @Override
    public void onTileSet(Tile tile) {
        setTile(tile);
    }

    public void setTile(Tile tile) {
        if (tile == null) {
            setLocation(new Point(0, 0));
        } else {
            setLocation(new Point(tile.getX() * mTileSize, tile.getY() * mTileSize));
        }
    }
}
