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

import com.wolfetones.physics.geometry.Plane;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Rendering helper methods.
 */
public class RenderUtils {
    public static final double CAMERA_HEIGHT_FROM_SCREEN = 1250;

    /**
     * Calculates the distance of a point from the center of the screen from a point at a depth in the world.
     *
     * @param worldDistance distance of the world point from (0, 0)
     * @param depth depth of the point in the world
     * @return distance of point on screen
     */
    private static double screenDistance(double worldDistance, double depth) {
        return CAMERA_HEIGHT_FROM_SCREEN * worldDistance / (-depth + CAMERA_HEIGHT_FROM_SCREEN);
    }

    /**
     * Projects a 3d point onto the screen, returning the projected point's 2d coordinates.
     *
     * @param point point to project
     * @return 2d coordinates of projected point
     */
    public static Point2d projectToScreen(Point3d point) {
        Point2d projection = new Point2d();

        projection.x = screenDistance(point.x, point.z);
        projection.y = screenDistance(point.y, point.z);

        return projection;
    }

    /**
     * Creates a plane at a point with a normal facing the Z-axis,
     * perpendicular to the line from the camera to the point.
     * <p>
     * Used to create planes for constraints that keep particles within the screen viewable area.
     *
     * <pre>
     * Viewed in 2D:
     *         Z-axis
     *
     *           D
     *           | \
     *           |   \               p
     *           |     \           p
     *           |       \       p
     *           |         N   p
     * ----------------------P    X/Y axis
     *           |         p
     *           |       p
     *           |     p
     *           |   p
     *           | p
     *           C
     *
     * Where
     *     C = Camera
     *     P = Point provided (on axis, z = 0)
     *     p = Plane created
     *     N = Normal
     *     D = Point at depth on Z axis
     * </pre>
     *
     * @param point point on plane
     * @return generated plane
     */
    public static Plane getPlaneFromCameraToPoint(Point2d point) {
        double depth = -Math.pow(point.distance(new Point2d()), 2) / CAMERA_HEIGHT_FROM_SCREEN;

        Point3d inner = new Point3d(0, 0, depth);
        Vector3d normal = new Vector3d();

        Point3d point3d = new Point3d(point.x, point.y, 0);
        normal.sub(inner, point3d);

        return new Plane(point3d, normal);
    }
}
