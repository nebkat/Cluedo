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

package com.wolfetones.cluedo.ui.component;

import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Component for displaying images.
 */
public class ImageComponent extends JComponent {
    protected BufferedImage mImage;

    /**
     * Constructs a new {@code ImageComponent} with the image's default width/height scaled relative to the screen size.
     *
     * @param image the image to display
     */
    public ImageComponent(BufferedImage image) {
        this(image, Config.screenRelativeSize(image.getWidth()), Config.screenRelativeSize(image.getHeight()));
    }

    /**
     * Constructs a new {@code ImageComponent}, scaling the image to the specified width and maintaining aspect ratio.
     *
     * @param image the image to display
     * @param width the width of the image
     */
    public ImageComponent(BufferedImage image, int width) {
        this(image, width, width * image.getHeight() / image.getWidth());
    }

    /**
     * Constructs a new {@code ImageComponent}, scaling the image to the specified dimensions.
     *
     * @param image the image to display
     * @param width the width of the image
     * @param height the height of the image
     */
    public ImageComponent(BufferedImage image, int width, int height) {
        super();

        mImage = ImageUtils.getScaledImage(image, width, height);

        setPreferredSize(new Dimension(width, height));
        setSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
    }

    @Override
    public void paintComponent(Graphics g) {
        Util.setHighQualityRenderingHints(g);

        g.drawImage(mImage, 0, 0, null);
    }
}
