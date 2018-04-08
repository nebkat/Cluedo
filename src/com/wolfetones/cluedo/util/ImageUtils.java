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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching images.
 */
public class ImageUtils {
    private static Map<String, SoftReference<BufferedImage>> sCache = new HashMap<>();

    private static String getCacheName(BufferedImage image) {
        for (Map.Entry<String, SoftReference<BufferedImage>> entry : sCache.entrySet()) {
            if (entry.getValue().get() == image) {
                return entry.getKey();
            }
        }

        return Integer.toString(image.getData().hashCode());
    }

    public static BufferedImage loadImage(String file) {
        // Check if cache exists
        if (sCache.containsKey(file)) {
            BufferedImage cachedImage = sCache.get(file).get();
            if (cachedImage != null) {
                return cachedImage;
            }
        }

        // Load image
        BufferedImage image;
        try {
            image = ImageIO.read(Util.class.getClassLoader().getResourceAsStream(file));
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Could not load image '" + file + "'");
        }

        // Ensure optimal color model
        image = getCompatibleImage(image);

        // Place in cache
        sCache.put(file, new SoftReference<>(image));

        return image;
    }

    public static BufferedImage getScaledImage(BufferedImage image, int width) {
        return getScaledImage(image, width, width * image.getHeight() / image.getWidth());
    }

    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        // Check if cache exists
        String cacheName = getCacheName(image) + "." + width + "x" + height;
        if (sCache.containsKey(cacheName)) {
            BufferedImage cachedImage = sCache.get(cacheName).get();
            if (cachedImage != null) {
                return cachedImage;
            }
        }

        // Scale to target dimensions
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(
                (double) width / image.getWidth(),
                (double) height / image.getHeight());

        AffineTransformOp scaleTransformOp = new AffineTransformOp(scaleTransform,
                AffineTransformOp.TYPE_BICUBIC);

        BufferedImage scaledImage = scaleTransformOp.filter(image, new BufferedImage(width, height, image.getType()));

        // Ensure optimal color model
        scaledImage = getCompatibleImage(scaledImage);

        // Place in cache
        sCache.put(cacheName, new SoftReference<>(scaledImage));

        return scaledImage;
    }

    public static BufferedImage getColorConvertedImage(BufferedImage image, int colorSpace) {
        // Check if cache exists
        String cacheName = getCacheName(image) + "." + colorSpace;
        if (sCache.containsKey(cacheName)) {
            BufferedImage cachedImage = sCache.get(cacheName).get();
            if (cachedImage != null) {
                return cachedImage;
            }
        }

        ColorConvertOp colorConvertOp = new ColorConvertOp(ColorSpace.getInstance(colorSpace), null);

        BufferedImage convertedImage = colorConvertOp.filter(image, new BufferedImage(image.getWidth(), image.getHeight(), image.getType()));

        // Ensure optimal color model
        convertedImage = getCompatibleImage(convertedImage);

        sCache.put(cacheName, new SoftReference<>(convertedImage));

        return convertedImage;
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
}
