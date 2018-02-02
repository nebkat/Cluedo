package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;

public abstract class Token extends Card {
    private TokenOccupiableTile mTile;
    private TokenTileListener mTileListner;

    Token(int id, String name) {
        super(id, name);
    }

    public void setTile(Tile tile) {
        if (!(tile instanceof TokenOccupiableTile)) {
            throw new IllegalStateException("Tile " + tile + " is not of class TokenOccupiableTile");
        } else if (!validTile(tile)) {
            throw new IllegalStateException("Tile " + tile + " is not a valid tile for " + this.getClass().getSimpleName());
        }

        if (mTile != null) {
            mTile.setToken(null);
        }

        mTile = (TokenOccupiableTile) tile;
        mTile.setToken(this);

        if (mTileListner != null) {
            mTileListner.onTileSet(tile);
        }
    }

    public TokenOccupiableTile getTile() {
        return mTile;
    }

    public void setTileListener(TokenTileListener listener) {
        mTileListner = listener;
    }

    protected abstract boolean validTile(Tile t);
}
