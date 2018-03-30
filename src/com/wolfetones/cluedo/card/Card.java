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
public abstract class Card {
    private String mName;

    private String mCardImage;

    Card(String name, String resourceName) {
        mName = name;

        if (resourceName != null) {
            mCardImage = "card-" + getCardImageSuffix() + "-" + resourceName + ".png";
        }
    }

    protected abstract String getCardImageSuffix();

    public String getName() {
        return mName;
    }

    public String getShortName() {
        return mName.replace(" ", "").replace(".", "").toLowerCase();
    }

    public BufferedImage getCardImage() {
        return ImageUtils.loadImage(mCardImage);
    }

    public static BufferedImage getCardBackImage() {
        return ImageUtils.loadImage("card-back.png");
    }

    public static BufferedImage getCardHighlightOverlayImage() {
        return ImageUtils.loadImage("card-highlight-overlay.png");
    }

    public static BufferedImage getCardSelectedOverlayImage() {
        return ImageUtils.loadImage("card-selected-overlay.png");
    }

    public static BufferedImage getCardCorrectOverlayImage() {
        return ImageUtils.loadImage("card-correct-overlay.png");
    }

    public static BufferedImage getCardIncorrectOverlayImage() {
        return ImageUtils.loadImage("card-incorrect-overlay.png");
    }
}
