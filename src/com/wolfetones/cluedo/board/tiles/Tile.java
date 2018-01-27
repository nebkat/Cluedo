package com.wolfetones.cluedo.board.tiles;

public abstract class Tile {
    protected int mX;
    protected int mY;

    protected Tile mLeft;
    protected Tile mUp;
    protected Tile mRight;
    protected Tile mDown;

    public Tile(int x, int y) {
        mX = x;
        mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public Tile getLeft() {
        return mLeft;
    }

    public Tile getUp() {
        return mUp;
    }

    public Tile getRight() {
        return mRight;
    }

    public Tile getDown() {
        return mDown;
    }

    public void setTiles(Tile left, Tile up, Tile right, Tile down) {
        mLeft = left;
        mUp = up;
        mRight = right;
        mDown = down;
    }
}
