package com.wolfetones.cluedo.card;

import java.awt.Color;

/**
 * Suspect
 */
public class Suspect extends Token {
    private Color mColor;
    private boolean mMovedSinceLastTurn = false;

    public Suspect(int id, String name, Color color) {
        super(id, name);

        mColor = color;
    }

    public Color getColor() {
        return mColor;
    }

    public void setMovedSinceLastTurn(boolean moved) {
        mMovedSinceLastTurn = moved;
    }

    public boolean getMovedSinceLastTurn() {
        return mMovedSinceLastTurn;
    }
}
