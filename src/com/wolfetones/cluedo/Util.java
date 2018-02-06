package com.wolfetones.cluedo;

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
}
