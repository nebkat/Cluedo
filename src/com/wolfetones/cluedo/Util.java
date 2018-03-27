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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
            y += (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        }
        g.drawString(s, x, y);
    }

    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        AffineTransform scaleTransform = AffineTransform.getScaleInstance((double) width / image.getWidth(), (double) height / image.getHeight());

        return new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BICUBIC).filter(
                image,
                new BufferedImage(width, height, image.getType()));
    }

    private static BufferedImage getCompatibleImage(BufferedImage image) {
        GraphicsConfiguration defaultGraphicsConfiguration = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        // If image is already using the recommended color model return it
        if (defaultGraphicsConfiguration.getColorModel().equals(image.getColorModel())) {
            return image;
        }

        // Create new image
        BufferedImage compatibleImage = defaultGraphicsConfiguration.createCompatibleImage(
                image.getWidth(),
                image.getHeight(),
                image.getTransparency());

        // Render to new image
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return compatibleImage;
    }

    public static Color getColorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static BufferedImage loadImage(String image) {
        try {
            return getCompatibleImage(ImageIO.read(Util.class.getClassLoader().getResourceAsStream(image)));
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not load image '" + image + "'");
        }
    }
}
