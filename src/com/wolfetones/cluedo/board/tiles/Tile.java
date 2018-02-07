package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.ui.TileComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Base tile class, representing a tile on the board
 */
public abstract class Tile {
    /**
     * Tile Coordinates
     */
    protected int mX;
    protected int mY;

    /**
     * Neighbouring tiles
     */
    protected Tile mLeft;
    protected Tile mUp;
    protected Tile mRight;
    protected Tile mDown;

    private TileComponent mButton;

    /**
     * Base tile constructor.
     *
     * @param x X coordinate of this tile.
     * @param y Y coordinate of this tile.
     */
    Tile(int x, int y) {
        mX = x;
        mY = y;
    }

    /**
     * Returns the X coordinate of this tile.
     *
     * @return the X coordinate of this tile.
     */
    public int getX() {
        return mX;
    }

    /**
     * Returns the Y coordinate of this tile.
     *
     * @return the Y coordinate of this tile.
     */
    public int getY() {
        return mY;
    }

    /**
     * Returns the {@code Tile} to the left of this tile.
     *
     * @return the {@code Tile} to the left of this tile.
     */
    public Tile getLeft() {
        return mLeft;
    }

    /**
     * Returns the {@code Tile} above this tile.
     *
     * @return the {@code Tile} above this tile.
     */
    public Tile getUp() {
        return mUp;
    }

    /**
     * Returns the {@code Tile} to the right of this tile.
     *
     * @return the {@code Tile} to the right of this tile.
     */
    public Tile getRight() {
        return mRight;
    }

    /**
     * Returns the {@code Tile} below this tile.
     *
     * @return the {@code Tile} below this tile.
     */
    public Tile getDown() {
        return mDown;
    }

    /**
     * Sets the neighbouring {@code Tile}s.
     * @param left the {@code Tile} to the left of this tile.
     * @param up the {@code Tile} above this tile.
     * @param right the {@code Tile} to the right of this tile.
     * @param down the {@code Tile} below this tile.
     */
    public void setNeighbours(Tile left, Tile up, Tile right, Tile down) {
        mLeft = left;
        mUp = up;
        mRight = right;
        mDown = down;
    }

    public TileComponent getButton() {
        return mButton;
    }

    public void setButton(TileComponent button) {
        mButton = button;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + mX + ", " + mY + "]";
    }
}
