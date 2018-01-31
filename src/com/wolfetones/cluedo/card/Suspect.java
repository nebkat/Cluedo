package com.wolfetones.cluedo.card;

import java.awt.*;

public class Suspect extends Card {
    private Color mColor;

    public Suspect(int id, String name, Color color) {
        super(id, name);

        mColor = color;
    }

    public Color getColor() {
        return mColor;
    }
}
