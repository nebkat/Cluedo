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

import com.wolfetones.cluedo.config.Config;
import com.wolfetones.physics.geometry.Plane;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class RenderUtils {
    public static final double CAMERA_HEIGHT_FROM_SCREEN = Config.screenRelativeSize(1250);

    public static Plane getPlaneFromCameraToPoint(Point2d point) {
        double depth = -Math.pow(point.distance(new Point2d()), 2) / CAMERA_HEIGHT_FROM_SCREEN;

        Point3d inner = new Point3d(0, 0, depth);
        Vector3d normal = new Vector3d();

        Point3d point3d = new Point3d(point.x, point.y, 0);
        normal.sub(inner, point3d);

        return new Plane(point3d, normal);
    }

    public static double screenDistance(double worldDistance, double depth) {
        return CAMERA_HEIGHT_FROM_SCREEN * worldDistance / (-depth + CAMERA_HEIGHT_FROM_SCREEN);
    }
}
