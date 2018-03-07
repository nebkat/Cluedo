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

public class Ray3d extends Point3d {
    private Vector3d direction;

    public Ray3d(Point3d origin, Vector3d direction) {
        super(origin);
        this.direction = new Vector3d(direction);
        this.direction.normalize();
    }

    public Ray3d(Point3d start, Point3d end) {
        super(start);

        this.direction = new Vector3d();
        this.direction.sub(start, end);
    }

    public Point3d getPointAtDistance(double distance) {
        Point3d point = new Point3d();
        point.scaleAdd(distance, this, direction);
        return point;
    }

    public Vector3d getDirection() {
        return new Vector3d(direction);
    }
}
