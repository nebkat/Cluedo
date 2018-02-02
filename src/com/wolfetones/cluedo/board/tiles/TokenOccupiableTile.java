package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Token;

public abstract class TokenOccupiableTile extends Tile {
    protected Token mToken;

    public TokenOccupiableTile(int x, int y) {
        super(x, y);
    }

    public void setToken(Token token) {
        mToken = token;
    }

    public Token getToken() {
        return mToken;
    }

    public boolean isOccupied() {
        return mToken != null;
    }
}
