package com.wolfetones.cluedo.card;

public class Suspect extends Card {
    private String mColor;

    public Suspect(int id, String name, String color) {
        super(id, name);

        mColor = color;
    }

    public String getColor() {
        return mColor;
    }
}
