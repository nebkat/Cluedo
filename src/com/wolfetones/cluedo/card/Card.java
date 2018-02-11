package com.wolfetones.cluedo.card;

import javax.swing.*;

public abstract class Card {
    private int mId;
    private String mName;

    private ImageIcon mCardImage;

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
