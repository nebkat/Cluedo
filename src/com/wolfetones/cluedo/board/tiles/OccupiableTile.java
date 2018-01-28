package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.board.PlayerOccupiable;
import com.wolfetones.cluedo.game.Player;

public abstract class OccupiableTile extends Tile implements PlayerOccupiable {
    protected Player mPlayer = null;

    public OccupiableTile(int x, int y) {
        super(x, y);
    }

    public void occupy(Player player) {
        mPlayer = player;
    }

    public void leave(Player player) {
        if (mPlayer == player) {
            mPlayer = null;
        }
    }

    public void setPlayer(Player player) {
        mPlayer = player;
    }

    public boolean isFullyOccupied() {
        return mPlayer != null;
    }
}
