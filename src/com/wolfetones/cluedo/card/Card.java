package com.wolfetones.cluedo.card;

public abstract class Card {
    private int mId;
    private String mName;

    public Card(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }
}
