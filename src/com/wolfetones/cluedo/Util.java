package com.wolfetones.cluedo;

public class Util {
    public static String implode(String[] items, String join) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            result.append(items[i]);
            if (i < items.length - 1) {
                result.append(join);
            }
        }
        return result.toString();
    }
}
