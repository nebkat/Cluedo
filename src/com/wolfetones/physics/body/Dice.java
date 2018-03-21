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

import com.wolfetones.physics.Particle;
import com.wolfetones.physics.RenderUtils;
import com.wolfetones.physics.VectorUtils;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class Dice extends Cube {
    private static final int[] FACE_VALUES = {1, 6, 3, 4, 2, 5};

    private Color faceColor = Color.WHITE;
    private Color borderColor = Color.BLACK;
    private Color dotColor = Color.BLACK;

    private static final Point2d[][] DICE_DOTS = {
            {
                    new Point2d(0.5, 0.5)
            },
            {
                    new Point2d(0.80, 0.20), new Point2d(0.20, 0.80)
            },
            {
                    new Point2d(0.80, 0.20), new Point2d(0.5, 0.5), new Point2d(0.20, 0.80)
            },
            {
                    new Point2d(0.20, 0.20), new Point2d(0.20, 0.80), new Point2d(0.80, 0.20), new Point2d(0.80, 0.80)
            },
            {
                    new Point2d(0.20, 0.20), new Point2d(0.20, 0.80), new Point2d(0.5, 0.5), new Point2d(0.80, 0.20), new Point2d(0.80, 0.80)
            },
            {
                    new Point2d(0.20, 0.20), new Point2d(0.20, 0.80), new Point2d(0.20, 0.5), new Point2d(0.80, 0.5), new Point2d(0.80, 0.20), new Point2d(0.80, 0.80)
            }
    };

    public Dice(Point3d center, double sideLength) {
        super(center, sideLength);
    }

    public int getHighestZFace() {
        int highestFace = -1;
        double highestTotalZ = 0;

        double totalZ;
        for (int i = 0; i < 6; i++) {
            totalZ = 0;
            for (int j = 0; j < 4; j++) {
                totalZ += faces[i][j].z;
            }

            if (highestFace < 0 || totalZ > highestTotalZ) {
                highestTotalZ = totalZ;
                highestFace = i;
            }
        }

        return highestFace;
    }

    public int getValue() {
        return FACE_VALUES[getHighestZFace()];
    }

    public void draw(Graphics2D g) {
        Path2D.Double polygon = new Path2D.Double();

        for (int f = 0; f < 6; f++) {
            Particle[] face = faces[f];

            // Only draw front facing faces
            if (!isFrontFace(face)) continue;

            /*
             * Draw face
             */
            polygon.reset();
            for (Particle particle : face) {
                Point2d p = RenderUtils.projectToScreen(particle);

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
            Vector3d width = new Vector3d();
            Vector3d height = new Vector3d();
            width.sub(face[1], face[0]);
            height.sub(face[2], face[1]);

            Vector3d a = new Vector3d();
            Vector3d b = new Vector3d();
            a.scale(0.125, width);
            b.scale(0.125, height);

            int value = FACE_VALUES[f];
            for (int d = 0; d < value; d++) {
                polygon.reset();

                Point2d dotCoordinates = DICE_DOTS[value - 1][d];
                Point3d xO = new Point3d();
                Point3d yO = new Point3d();

                xO.scale(dotCoordinates.x, width);
                yO.scale(dotCoordinates.y, height);

                Point3d base = new Point3d(face[0]);
                base.add(xO);
                base.add(yO);

                Point3d pos = new Point3d();
                Vector3d i = new Vector3d();
                Vector3d j = new Vector3d();

                for (double c = 0; c < 1; c += 0.05) {
                    pos.set(base);

                    double angle = 2 * Math.PI * c;

                    i.scale(Math.cos(angle), a);
                    j.scale(Math.sin(angle), b);

                    pos.add(i);
                    pos.add(j);

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
