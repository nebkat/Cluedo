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
import com.wolfetones.cluedo.card.Suspect;

import java.awt.*;

public class SuspectTokenComponent extends TokenComponent {
    private static final float MARGIN_ACTIVE = 0.05f;
    private static final float MARGIN = 0.1f;

    public SuspectTokenComponent(Suspect suspect, int tileSize) {
        super(suspect, tileSize);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(((Suspect) mToken).getColor());
        g.fillOval((int)(getWidth() * 0.1), (int)(getHeight() * 0.1), (int)(getWidth() * 0.8), (int)(getHeight() * 0.8));

        g.setColor(Color.BLACK);
        Util.drawCenteredString(mToken.getName().substring(0, 1), getWidth(), getHeight(), g);
    }
}
