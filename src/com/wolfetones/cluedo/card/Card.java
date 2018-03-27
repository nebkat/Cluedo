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

import com.wolfetones.cluedo.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Base card class, representing a game card.
 */
public abstract class Card {
    private String mName;

    private BufferedImage mCardImage;

    private static BufferedImage sCardBackImage;
    private static BufferedImage sCardHighlightOverlayImage;
    private static BufferedImage sCardSelectedOverlayImage;

    Card(String name, String resourceName) {
        mName = name;

        if (resourceName != null) {
            String imageFile = "card-" + getCardImageSuffix() + "-" + resourceName + ".png";
            mCardImage = Util.loadImage(imageFile);
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
        return mCardImage;
    }

    public static BufferedImage getCardBackImage() {
        if (sCardBackImage != null) {
            return sCardBackImage;
        }

        sCardBackImage = Util.loadImage("card-back.png");

        return sCardBackImage;
    }

    public static BufferedImage getCardHighlightOverlayImage() {
        if (sCardHighlightOverlayImage != null) {
            return sCardHighlightOverlayImage;
        }

        sCardHighlightOverlayImage = Util.loadImage("card-highlight-overlay.png");

        return sCardHighlightOverlayImage;
    }

    public static BufferedImage getCardSelectedOverlayImage() {
        if (sCardSelectedOverlayImage != null) {
            return sCardSelectedOverlayImage;
        }

        sCardSelectedOverlayImage = Util.loadImage("card-selected-overlay.png");

        return sCardSelectedOverlayImage;
    }
}
