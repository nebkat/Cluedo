/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.card.Weapon;

import java.awt.*;

public class WeaponTokenComponent extends TokenComponent {
    private static final float MARGIN = 0.1f;

    public WeaponTokenComponent(Weapon weapon, int tileSize) {
        super(weapon, tileSize);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillOval(
                (int) (getWidth() * MARGIN),
                (int) (getHeight() * MARGIN),
                (int) (getWidth() * (1 - 2 * MARGIN)),
                (int) (getHeight() * (1 - 2 * MARGIN)));

        g.setColor(Color.WHITE);
        Util.drawCenteredString(mToken.getName().substring(0, 1), getWidth(), getHeight(), g);
    }
}
