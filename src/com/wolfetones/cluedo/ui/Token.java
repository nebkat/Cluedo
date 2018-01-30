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

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval((int)(getWidth() * 0.1), (int)(getHeight() * 0.1), (int)(getWidth() * 0.8), (int)(getHeight() * 0.8));
    }
}
