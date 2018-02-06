package com.wolfetones.cluedo.card;

public abstract class Card {
    private int mId;
    private String mName;

    Card(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getShortName() {
        return mName.replace(" ", "").replace(".", "").toLowerCase();
    }
}
