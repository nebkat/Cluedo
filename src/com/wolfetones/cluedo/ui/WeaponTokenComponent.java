package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.card.Weapon;

import java.awt.*;

public class WeaponTokenComponent extends TokenComponent {
    public WeaponTokenComponent(Weapon weapon, int tileSize) {
        super(weapon, tileSize);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval((int)(getWidth() * 0.1), (int)(getHeight() * 0.1), (int)(getWidth() * 0.8), (int)(getHeight() * 0.8));

        g.setColor(Color.WHITE);
        Util.drawCenteredString(mToken.getName().substring(0, 1), getWidth(), getHeight(), g);
    }
}
