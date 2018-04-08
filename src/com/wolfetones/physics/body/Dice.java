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

package com.wolfetones.physics.body;

import com.wolfetones.physics.RenderUtils;
import com.wolfetones.physics.geometry.Face;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Dice.
 */
public class Dice extends Cube {
    /**
     * Number of dots on each face of the cube.
     */
    public static final int[] FACE_VALUES = {1, 6, 3, 4, 2, 5};

    private Color faceColor = Color.WHITE;
    private Color borderColor = Color.BLACK;
    private Color dotColor = Color.BLACK;

    /**
     * Locations of dots on faces for each dice value.
     */
    private static final double[][][] DICE_DOTS = {
            {
                    {0.5, 0.5}
            },
            {
                    {0.80, 0.20}, {0.20, 0.80}
            },
            {
                    {0.80, 0.20}, {0.5, 0.5}, {0.20, 0.80}
            },
            {
                    {0.20, 0.20}, {0.20, 0.80}, {0.80, 0.20}, {0.80, 0.80}
            },
            {
                    {0.20, 0.20}, {0.20, 0.80}, {0.5, 0.5}, {0.80, 0.20}, {0.80, 0.80}
            },
            {
                    {0.20, 0.20}, {0.20, 0.80}, {0.20, 0.5}, {0.80, 0.5}, {0.80, 0.20}, {0.80, 0.80}
            }
    };

    public Dice(Point3d center, double sideLength) {
        super(center, sideLength);
    }

    /**
     * Returns the index of the face of the dice with the highest Z value.
     *
     * @return the index of the face of the dice with the highest Z value.
     */
    public int getHighestZFace() {
        int highestFace = -1;
        double highestTotalZ = 0;

        // Calculate total Z instead of average Z
        double totalZ;
        for (int f = 0; f < 6; f++) {
            totalZ = 0;
            for (int p = 0; p < 4; p++) {
                totalZ += faces[f].getVertex(p).z;
            }

            if (highestFace < 0 || totalZ > highestTotalZ) {
                highestTotalZ = totalZ;
                highestFace = f;
            }
        }

        return highestFace;
    }

    /**
     * Returns the value of the highest face.
     *
     * @return the value of the highest face.
     */
    public int getValue() {
        return FACE_VALUES[getHighestZFace()];
    }

    /**
     * Draws the dice on the provided {@code Graphics} object.
     *
     * @param g graphics on which to draw the dice
     */
    public void draw(Graphics2D g) {
        Path2D.Double polygon = new Path2D.Double();

        for (int f = 0; f < 6; f++) {
            Face face = faces[f];

            // Only draw front facing faces
            if (!face.isFrontFace()) continue;

            /*
             * Draw face
             */
            polygon.reset();
            for (Point3d point : face) {
                Point2d p = RenderUtils.projectToScreen(point);

                if (polygon.getCurrentPoint() == null) {
                    polygon.moveTo(p.x, p.y);
                } else {
                    polygon.lineTo(p.x, p.y);
                }
            }
            polygon.closePath();

            // White background
            g.setColor(faceColor);
            g.fill(polygon);

            // Black border
            g.setColor(borderColor);
            g.draw(polygon);

            /*
             * Draw dots
             */
            Vector3d width = face.getVertexDelta(0, 1);
            Vector3d height = face.getVertexDelta(1, 2);

            Vector3d dotWidthRadius = new Vector3d();
            Vector3d dotHeightRadius = new Vector3d();
            dotWidthRadius.scale(0.125, width);
            dotHeightRadius.scale(0.125, height);

            // Draw the number of dots required for this face
            int value = FACE_VALUES[f];
            for (int d = 0; d < value; d++) {
                polygon.reset();

                double[] dotCoordinates = DICE_DOTS[value - 1][d];
                Point3d dotCenter = new Point3d(face.getVertex(0));
                dotCenter.scaleAdd(dotCoordinates[0], width, dotCenter);
                dotCenter.scaleAdd(dotCoordinates[1], height, dotCenter);

                Point3d pos = new Point3d();
                for (double c = 0; c < 1; c += 0.05) {
                    double angle = Math.toRadians(c * 360);

                    pos.set(dotCenter);
                    pos.scaleAdd(Math.cos(angle), dotWidthRadius, pos);
                    pos.scaleAdd(Math.sin(angle), dotHeightRadius, pos);

                    Point2d p = RenderUtils.projectToScreen(pos);

                    if (polygon.getCurrentPoint() == null) {
                        polygon.moveTo(p.x, p.y);
                    } else {
                        polygon.lineTo(p.x, p.y);
                    }
                }

                g.setColor(dotColor);
                g.fill(polygon);
            }
        }
    }
}
