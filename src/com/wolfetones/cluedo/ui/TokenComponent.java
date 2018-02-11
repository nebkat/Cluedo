package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.card.Token;

import javax.swing.*;
import java.awt.*;

public abstract class TokenComponent extends JComponent implements Token.TokenTileListener {
    private int mTileSize;
    protected Token mToken;

    protected TokenComponent(Token token, int tileSize) {
        mToken = token;
        mTileSize = tileSize;
        setSize(tileSize, tileSize);
        setTile(token.getTile());

        token.setTileListener(this);
    }

    public Token getToken() {
        return mToken;
    }

    @Override
    public void onTileSet(Tile tile) {
        setTile(tile);
    }

    public void setTile(Tile tile) {
        if (tile == null) {
            setLocation(new Point(0, 0));
        } else {
            setLocation(new Point(tile.getX() * mTileSize, tile.getY() * mTileSize));
        }
    }
}
