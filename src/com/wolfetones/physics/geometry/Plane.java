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

/**
 * Infinite geometric plane.
 */
public class Plane extends Point3d {
    private Vector3d normal;

    /**
     * Constructs a plane at the specified point with the specified normal.
     *
     * @param origin a point on the plane
     * @param normal the normal of the plane
     */
    public Plane(Point3d origin, Vector3d normal) {
        super(origin);
        this.normal = new Vector3d(normal);
        this.normal.normalize();
    }

    /**
     * Classifies a point as being in front of, behind, or on the plane.
     *
     * @param point the point to classify
     * @return {@code 1} for points in front of the plane, {@code -1} for points behind the plane, and {@code 0} for points on the plane
     */
    public int classifyPoint(Point3d point) {
        Vector3d delta = new Vector3d();
        delta.sub(point, this);
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

    /**
     * Gets the projection of the specified point on to the plane.
     *
     * @param point the point to project
     * @return the projection of the specified point on to the plane
     */
    public Point3d getProjectedPoint(Point3d point) {
        Vector3d delta = new Vector3d();
        delta.sub(this, point);

        double perpendicularDistance = normal.dot(delta);

        Point3d intersection = new Point3d();
        intersection.scaleAdd(perpendicularDistance, normal, point);

        return intersection;
    }

    /**
     * Gets the intersection point of the plane and the specified ray.
     *
     * @param ray the ray for which to calculate an intersection
     * @return the intersection point of the plane and the specified ray
     */
    public Point3d getIntersectionPoint(Ray3d ray) {
        Vector3d delta = new Vector3d();
        delta.sub(this, ray);

        double perpendicularDistance = normal.dot(delta);
        double denominator = normal.dot(ray.getDirection());

        // Ray and plane are parallel
        if (denominator == 0) {
            return null;
        }

        return ray.getPointAtDistance(perpendicularDistance / denominator);
    }
}
