package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.Tile;

import java.awt.*;

public class Suspect extends Token {
    private Color mColor;

    public Suspect(int id, String name, Color color) {
        super(id, name);

        mColor = color;
    }

    public Color getColor() {
        return mColor;
    }

    @Override
    protected boolean validTile(Tile t) {
        return true;
    }
}
