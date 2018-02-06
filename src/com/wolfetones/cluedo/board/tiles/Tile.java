package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.ui.TileComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class Tile {
    protected int mX;
    protected int mY;

    protected Tile mLeft;
    protected Tile mUp;
    protected Tile mRight;
    protected Tile mDown;

    private TileComponent mButton;

    protected List<Tile> mNeighbours = new ArrayList<>();
    protected List<TokenOccupiableTile> mTokenTraversableNeighbours = new ArrayList<>();

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

    public void setNeighbours(Tile left, Tile up, Tile right, Tile down) {
        mLeft = left;
        mUp = up;
        mRight = right;
        mDown = down;

        mNeighbours.add(left);
        mNeighbours.add(up);
        mNeighbours.add(right);
        mNeighbours.add(down);

        if (left instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) left);
        if (up instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) up);
        if (right instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) right);
        if (down instanceof CorridorTile) mTokenTraversableNeighbours.add((TokenOccupiableTile) down);
    }

    public List<Tile> getNeighbours() {
        return mNeighbours;
    }

    public List<TokenOccupiableTile> getTokenTraversableNeighbours() {
        return mTokenTraversableNeighbours;
    }

    public TileComponent getButton() {
        return mButton;
    }

    public void setButton(TileComponent button) {
        mButton = button;
    }

    @Override
    public String toString() {
        return "[" + mX + ", " + mY + "]";
    }
}
