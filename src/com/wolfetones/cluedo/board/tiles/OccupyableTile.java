package com.wolfetones.cluedo.board.tiles;

public abstract class OccupyableTile extends Tile {
    protected boolean mOccupied = false;

    public OccupyableTile(int x, int y) {
        super(x, y);
    }

    public void setOccupied(boolean occupied) {
        mOccupied = occupied;
    }

    public boolean isOccupied() {
        return mOccupied;
    }
}
