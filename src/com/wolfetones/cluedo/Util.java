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
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(s)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(s, x, y);
    }
}
