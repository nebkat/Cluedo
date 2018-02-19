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

package com.wolfetones.cluedo;

import java.awt.*;
import java.util.Collection;

public class Util {
    public static String implode(Collection<String> items, String join) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (String s : items) {
            result.append(s);
            if (i < items.size() - 1) {
                result.append(join);
            }
            i++;
        }
        return result.toString();
    }

    public static void drawCenteredString(String s, int w, int h, Graphics g) {
        drawCenteredString(s, 0, 0, w, h, g);
    }

    public static void drawCenteredString(String s, int x, int y, int w, int h, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        x += (w - fm.stringWidth(s)) / 2;
        y += (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(s, x, y);
    }

    public static double easeInOutQuint(double t) {
        return t < .5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    }
}
