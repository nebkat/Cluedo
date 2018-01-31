package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.Tile;

import javax.swing.*;
import java.awt.*;

public abstract class Token extends JComponent {
    private int mTileSize;
    private Tile mTile;

    public Token(int tileSize) {
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
    }

    public void setTile(Tile t) {
        mTile = t;
        setPosition(t.getX(), t.getY());
    }

    public Tile getTile() {
        return mTile;
    }

    public void setPosition(int x, int y) {
        setLocation(new Point(x * mTileSize, y * mTileSize));
    }
}
