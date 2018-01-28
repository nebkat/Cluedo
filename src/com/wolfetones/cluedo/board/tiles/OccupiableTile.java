package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.game.Player;

public abstract class OccupiableTile extends Tile {
    protected Player mPlayer = null;

    public OccupiableTile(int x, int y) {
        super(x, y);
    }

    public void setPlayer(Player player) {
        mPlayer = player;
    }

    public boolean isOccupied() {
        return mPlayer != null;
    }
}
