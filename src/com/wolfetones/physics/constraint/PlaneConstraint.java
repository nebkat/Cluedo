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

package com.wolfetones.physics.constraint;

import com.wolfetones.physics.geometry.Plane;
import com.wolfetones.physics.Particle;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Particle constraint that keeps a particle in front of a given plane.
 */
public class PlaneConstraint extends Constraint {
    private Plane plane;
    private double restitution;
    private double friction;

    /**
     * Constructs a plane constraint with the specified plane.
     *
     * @param plane plane
     * @param restitution the coefficient of restitution, applied to perpendicular forces of objects colliding with the plane
     * @param friction the coefficient of friction, applied to lateral forces of objects colliding with the plane
     */
    public PlaneConstraint(Plane plane, double restitution, double friction) {
        super();

        this.plane = plane;
        this.restitution = restitution;
        this.friction = friction;
    }

    /**
     * Applies the constraint to the specified particle.
     * <p>
     * If the particle is in front of the plane, nothing occurs.
     * <p>
     * If the particle is behind the plane, the particle is moved to the closest point on the plane. The particle's
     * velocity perpendicular to the plane is inverted and scaled by the constraint's restitution coefficient, and
     * the velocity parallel to the plane is scaled by the friction coefficient.
     *
     * @param particle the particle to apply the constraint to
     */
    @Override
    public void apply(Particle particle) {
        if (plane.classifyPoint(particle) >= 0) return;

        // Project current and previous point to plane
        Point3d newProjection = plane.getProjectedPoint(particle);
        Point3d previousProjection = plane.getProjectedPoint(particle.previousPosition);

        // Parallel motion along plane
        Vector3d parallelMotion = new Vector3d();
        parallelMotion.sub(newProjection, previousProjection);

        // Perpendicular motion towards plane
        Vector3d perpendicularMotion = new Vector3d();
        perpendicularMotion.sub(particle, particle.previousPosition);
        perpendicularMotion.sub(parallelMotion);

        // Move particle back to projection point (closest non-colliding point)
        particle.set(newProjection);
        particle.previousPosition.set(newProjection);

        // Scale perpendicular motion by restitution
        perpendicularMotion.scale(this.restitution);
        particle.previousPosition.add(perpendicularMotion);

        // Scale parallel motion by friction
        parallelMotion.scale(this.friction);
        particle.previousPosition.sub(parallelMotion);
    }
}
