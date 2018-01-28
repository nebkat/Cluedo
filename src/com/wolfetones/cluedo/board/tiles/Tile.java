package com.wolfetones.cluedo.board.tiles;

import javax.swing.JButton;
import java.util.ArrayList;
import java.util.List;

public abstract class Tile {
    protected int mX;
    protected int mY;

    private Tile mLeft;
    private Tile mUp;
    private Tile mRight;
    private Tile mDown;

    private JButton mButton;

    private List<Tile> mNeighbours = new ArrayList<>();

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
    }

    public List<Tile> getNeighbours() {
        return mNeighbours;
    }

    public JButton getButton() {
        return mButton;
    }

    public void setButton(JButton button) {
        mButton = button;
    }
}
