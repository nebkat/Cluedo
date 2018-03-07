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

package com.wolfetones.physics.geometry;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Plane extends Point3d {
    private Vector3d normal;

    public Plane(Point3d origin, Vector3d normal) {
        super(origin);
        this.normal = new Vector3d(normal);
        this.normal.normalize();
    }

    public int classifyPoint(Point3d point) {
        Vector3d delta = new Vector3d(point);
        delta.sub(this);
        delta.normalize();

        double d = delta.dot(normal);
        if (d < 0) {
            return -1;
        } else if (d > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public Point3d getProjectedPoint(Point3d point) {
        Vector3d delta = new Vector3d();
        delta.sub(this, point);

        double perpendicularDistance = normal.dot(delta);
        double denominator = normal.lengthSquared();

        Point3d intersection = new Point3d();
        intersection.scaleAdd(perpendicularDistance / denominator, normal, point);

        return intersection;
    }

    public Point3d getIntersectionPoint(Ray3d ray) {
        Vector3d delta = new Vector3d();
        delta.sub(this, ray);

        double perpendicularDistance = normal.dot(delta);
        double denominator = normal.dot(ray.getDirection());

        return ray.getPointAtDistance(perpendicularDistance / denominator);
    }
}
