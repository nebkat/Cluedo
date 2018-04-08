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

package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.util.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * Base card class, representing a game card.
 */
public abstract class Card implements Comparable<Card> {
    private static final Class[] TYPE_ORDER = {Suspect.class, Weapon.class, Room.class};

    private String mName;
    private String[] mSearchNames;
    private String mCardImage;

    Card(String name, String[] searchNames, String resourceName) {
        mName = name;

        mSearchNames = searchNames;

        if (resourceName != null) {
            mCardImage = "cards/card-" + getCardImageSuffix() + "-" + resourceName + ".png";
        }
    }

    /**
     * Returns the suffix used for generating the card image filename.
     *
     * Overridden by subclasses.
     *
     * @return the suffix used for generating the card image filename
     */
    protected abstract String getCardImageSuffix();

    /**
     * Gets the name of this card.
     *
     * @return the card name
     */
    public String getName() {
        return mName;
    }

    public String[] getSearchNames() {
        return mSearchNames;
    }

    /**
     * Loads and returns the card image associated with this card.
     *
     * @return the card image associated with this card
     */
    public BufferedImage getCardImage() {
        return ImageUtils.loadImage(mCardImage);
    }

    public static BufferedImage getCardBackImage() {
        return ImageUtils.loadImage("cards/card-back.png");
    }

    public static BufferedImage getCardHighlightOverlayImage() {
        return ImageUtils.loadImage("cards/card-highlight-overlay.png");
    }

    public static BufferedImage getCardSelectedOverlayImage() {
        return ImageUtils.loadImage("cards/card-selected-overlay.png");
    }

    public static BufferedImage getCardCorrectOverlayImage() {
        return ImageUtils.loadImage("cards/card-correct-overlay.png");
    }

    public static BufferedImage getCardIncorrectOverlayImage() {
        return ImageUtils.loadImage("cards/card-incorrect-overlay.png");
    }

    /**
     * Gets the index of the class of the card in the card ordering array.
     *
     * Used to sort cards by suspect, weapon and room.
     *
     * @return the ordering index of the card
     */
    private int getTypeIndex() {
        for (int i = 0; i < TYPE_ORDER.length; i++) {
            if (TYPE_ORDER[i] == getClass()) {
                return i;
            }
        }
        return TYPE_ORDER.length;
    }

    private static int compare(Card a, Card b) {
        if (a.getClass() == b.getClass()) {
            return a.getName().compareTo(b.getName());
        }

        return Integer.compare(a.getTypeIndex(), b.getTypeIndex());
    }

    @Override
    public int compareTo(Card c) {
        return Card.compare(this, c);
    }
}
