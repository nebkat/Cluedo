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
import com.wolfetones.physics.spring.Spring;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public abstract class Body {
    protected List<Particle> particles = new ArrayList<>();
    protected List<Spring> springs = new ArrayList<>();

    public List<Particle> getParticles() {
        return particles;
    }

    public List<Spring> getSprings() {
        return springs;
    }

    public Point3d getCenter() {
        return VectorUtils.average(particles);
    }

    public void translate(Vector3d translate) {
        translate(translate, false);
    }

    public void translate(Vector3d translate, boolean updatePrevious) {
        for (Particle p : particles) {
            p.translate(translate, updatePrevious);
        }
    }

    public void transform(Matrix3d transform) {
        transform(transform, false);
    }

    public void transform(Matrix3d transform, boolean updatePrevious) {
        transform(transform, updatePrevious, getCenter());
    }

    public void transform(Matrix3d transform, boolean updatePrevious, Point3d transformOrigin) {
        for (Particle p : particles) {
            p.transform(transform, updatePrevious, transformOrigin);
        }
    }

    public void reset() {
        for (Particle p : particles) {
            p.reset();
        }
    }
}
