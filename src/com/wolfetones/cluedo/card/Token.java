package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;
import com.wolfetones.cluedo.game.Location;

public abstract class Token extends Card {
    private TokenOccupiableTile mTile;
    private TokenTileListener mTileListner;
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

    public interface TokenTileListener {
        void onTileSet(Tile tile);
    }
}
