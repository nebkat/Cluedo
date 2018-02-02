package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.card.Token;
import com.wolfetones.cluedo.card.TokenTileListener;

import javax.swing.*;
import java.awt.*;

public abstract class TokenComponent extends JComponent implements TokenTileListener {
    private int mTileSize;
    protected Token mToken;

    protected TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);

        token.setTileListener(this);
    }

    public Token getToken() {
        return mToken;
    }

    @Override
    public void onTileSet(Tile tile) {
        setPosition(tile.getX(), tile.getY());
    }

    public void setPosition(int x, int y) {
        setLocation(new Point(x * mTileSize, y * mTileSize));
    }
}
