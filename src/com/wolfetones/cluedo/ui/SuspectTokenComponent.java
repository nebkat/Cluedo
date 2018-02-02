package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Suspect;

import java.awt.*;

public class SuspectTokenComponent extends TokenComponent {
    public SuspectTokenComponent(Suspect suspect, int tileSize) {
        super(suspect, tileSize);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(((Suspect) mToken).getColor());
        g.fillOval((int)(getWidth() * 0.1), (int)(getHeight() * 0.1), (int)(getWidth() * 0.8), (int)(getHeight() * 0.8));
    }
}
