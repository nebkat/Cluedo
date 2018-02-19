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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Base card class, representing a game card.
 */
public abstract class Card {
    private int mId;
    private String mName;

    private BufferedImage mCardImage;

    private static BufferedImage sCardHighlightOverlayImage;
    private static BufferedImage sCardSelectedOverlayImage;

    Card(int id, String name, String cardImage) {
        mId = id;
        mName = name;

        if (cardImage != null) {
            try {
                mCardImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream(cardImage));
            } catch (IOException e) {
                System.err.println("Couldn't load card image for " + getClass().getSimpleName() + " " + getName() + " at resources/" + cardImage);
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getShortName() {
        return mName.replace(" ", "").replace(".", "").toLowerCase();
    }

    public BufferedImage getCardImage() {
        return mCardImage;
    }

    public static BufferedImage getCardHighlightOverlayImage() {
        if (sCardHighlightOverlayImage != null) {
            return sCardHighlightOverlayImage;
        }

        try {
            sCardHighlightOverlayImage = ImageIO.read(Card.class.getClassLoader().getResourceAsStream("card-highlight-overlay.png"));
        } catch (IOException e) {
        }

        return sCardHighlightOverlayImage;
    }

    public static BufferedImage getCardSelectedOverlayImage() {
        if (sCardSelectedOverlayImage != null) {
            return sCardSelectedOverlayImage;
        }

        try {
            sCardSelectedOverlayImage = ImageIO.read(Card.class.getClassLoader().getResourceAsStream("card-selected-overlay.png"));
        } catch (IOException e) {
        }

        return sCardSelectedOverlayImage;
    }
}
