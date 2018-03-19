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

package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScaledImageComponent extends JComponent {
    protected BufferedImage mImage;

    public ScaledImageComponent(BufferedImage image) {
        this(image, Config.screenRelativeSize(image.getWidth()), Config.screenRelativeSize(image.getHeight()));
    }

    public ScaledImageComponent(BufferedImage image, int width, int height) {
        super();

        mImage = Util.getScaledImage(image, width, height);

        setPreferredSize(new Dimension(width, height));
        setSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(mImage, 0, 0, null);
    }
}
