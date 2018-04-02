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

package com.wolfetones.cluedo.util;

import java.awt.*;
import java.util.Collection;

public class Util {
    public static String implode(Collection<?> items, String join) {
        return implode(items, join, join);
    }

    public static String implode(Collection<?> items, String join, String finalJoin) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Object s : items) {
            if (i > 0) {
                result.append(i == items.size() - 1 ? finalJoin : join);
            }
            result.append(s);
            i++;
        }
        return result.toString();
    }

    public static void drawCenteredString(String s, int x, int y, int w, int h, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        if (w >= 0) {
            x += (w - fm.stringWidth(s)) / 2;
        }
        if (h >= 0) {
            y += (h - fm.getDescent() + fm.getAscent()) / 2;
        }
        g.drawString(s, x, y);
    }

}
