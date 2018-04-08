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
import com.wolfetones.physics.VectorUtils;
import com.wolfetones.physics.Spring;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract body of particles connected by springs.
 */
public abstract class Body {
    protected List<Particle> particles = new ArrayList<>();
    protected List<Spring> springs = new ArrayList<>();

    /**
     * Gets the list of particles in this body.
     *
     * @return the list of particles in this body.
     */
    public List<Particle> getParticles() {
        return particles;
    }

    /**
     * Gets the list of springs in this body.
     *
     * @return the list of springs in this body.
     */
    public List<Spring> getSprings() {
        return springs;
    }

    /**
     * Gets the center coordinate of the body.
     *
     * By default returns the average value of all of the body's particles.
     *
     * @return the center coordinate of the body
     */
    public Point3d getCenter() {
        return VectorUtils.average(particles);
    }

    /**
     * Applies a translation to every particle in the body without updating the previous position.
     *
     * @see Body#translate(Vector3d, boolean)
     * @see Particle#translate(Vector3d, boolean)
     *
     * @param translate the direction in which to translate
     */
    public void translate(Vector3d translate) {
        translate(translate, false);
    }

    /**
     * Applies a translation to every particle in the body, optionally updating the previous position of the particles.
     *
     * @see Particle#translate(Vector3d, boolean)
     *
     * @param translate the direction in which to translate
     */
    public void translate(Vector3d translate, boolean updatePrevious) {
        for (Particle p : particles) {
            p.translate(translate, updatePrevious);
        }
    }

    /**
     * Applies a transformation to every particle in the body around the center of the body, without updating the previous position.
     *
     * @see Body#transform(Matrix3d, boolean)
     * @see Particle#transform(Matrix3d, Point3d)
     *
     * @param transform the transformation to apply
     */
    public void transform(Matrix3d transform) {
        transform(transform, false);
    }

    /**
     * Applies a transformation to every particle in the body around the center of the body, optionally updating the previous position of the particle.
     *
     * @see Body#transform(Matrix3d, boolean, Point3d)
     * @see Particle#transform(Matrix3d, boolean, Point3d)
     *
     * @param transform the transformation to apply
     * @param updatePrevious whether to update the previous position
     */
    public void transform(Matrix3d transform, boolean updatePrevious) {
        transform(transform, updatePrevious, getCenter());
    }

    /**
     * Applies a transformation to every particle in the body around the specified point, optionally updating the previous position of the particle.
     *
     * @see Particle#transform(Matrix3d, boolean, Point3d)
     *
     * @param transform the transformation to apply
     * @param updatePrevious whether to update the previous position
     * @param transformOrigin the point around which to perform the transformation
     */
    public void transform(Matrix3d transform, boolean updatePrevious, Point3d transformOrigin) {
        for (Particle p : particles) {
            p.transform(transform, updatePrevious, transformOrigin);
        }
    }

    /**
     * Places the body back at its initial position with 0 velocity.
     */
    public void reset() {
        for (Particle p : particles) {
            p.reset();
        }
    }
}
