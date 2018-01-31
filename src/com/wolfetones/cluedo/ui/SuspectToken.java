package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Suspect;

import java.awt.*;

public class SuspectToken extends Token {
    private Suspect mSuspect;

    public SuspectToken(int tileSize, Suspect suspect) {
        super(tileSize);

        mSuspect = suspect;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(mSuspect.getColor());
        g.fillOval((int)(getWidth() * 0.1), (int)(getHeight() * 0.1), (int)(getWidth() * 0.8), (int)(getHeight() * 0.8));
    }
}
