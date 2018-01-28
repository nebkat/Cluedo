package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.game.Player;

public abstract class OccupyableTile extends Tile {
    protected Player mPlayer = null;

    public OccupyableTile(int x, int y) {
        super(x, y);
    }

    public void setPlayer(Player player) {
        mPlayer = player;
    }

    public boolean isOccupied() {
        return mPlayer != null;
    }
}
