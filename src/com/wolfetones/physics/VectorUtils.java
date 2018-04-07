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

package com.wolfetones.physics;

import javax.vecmath.Point3d;

/**
 * Vector helper methods.
 */
public class VectorUtils {
    /**
     * Returns the average value of a set of points.
     *
     * @param points the points for which to calculate the average
     * @return the average value of the points provided
     */
    public static Point3d average(Point3d[] points) {
        Point3d average = new Point3d();
        for (Point3d point : points) {
            average.add(point);
        }
        average.scale(1.0 / points.length);

        return average;
    }

    /**
     * Returns the average value of a set of points.
     *
     * @param points the points for which to calculate the average
     * @return the average value of the points provided
     */
    public static Point3d average(Iterable<? extends Point3d> points) {
        Point3d average = new Point3d();
        int count = 0;
        for (Point3d point : points) {
            average.add(point);
            count++;
        }
        average.scale(1.0 / count);

        return average;
    }
}
