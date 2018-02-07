package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Tile that can hold a {@link Token}.
 */
public abstract class TokenOccupiableTile extends Tile {
    protected Token mToken;

    protected List<TokenOccupiableTile> mTokenTraversableNeighbours = new ArrayList<>();

    TokenOccupiableTile(int x, int y) {
        super(x, y);
    }

    /**
     * Sets the {@code Token} that is currently occupying this tile.
     *
     * @param token the {@code Token} that is currently occupying this tile.
     */
    public void setToken(Token token) {
        mToken = token;
    }

    /**
     * Gets the {@code Token} that is currently occupying this tile.
     *
     * @return the {@code Token} that is currently occupying this tile.
     */
    public Token getToken() {
        return mToken;
    }

    /**
     * Provides a list of all neighbouring tiles that can be reached by a {@code Token} from this tile.
     *
     * @return A list of
     */
    public List<TokenOccupiableTile> getTokenTraversableNeighbours() {
        return mTokenTraversableNeighbours;
    }

    /**
     * Returns {@code true} if this tile is occupied by a {@code Token}
     *
     * @return {@code true} if this tile is occupied by a {@code Token}
     */
    public boolean isOccupied() {
        return mToken != null;
    }

    /**
     * Returns {@code true} if this tile is not occupied by a {@code Token}
     *
     * @return {@code true} if this tile is not occupied by a {@code Token}
     */
    public boolean isFree() {
        return mToken == null;
    }
}
