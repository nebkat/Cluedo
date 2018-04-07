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

package com.wolfetones.physics.behavior;

import com.wolfetones.physics.Particle;

import javax.vecmath.Vector3d;

/**
 * Particle behavior that applies a constant force in a given direction to a particle.
 */
public class ConstantForceBehavior extends Behavior {
    private Vector3d force;

    /**
     * Constructs a constant force behavior with the given force.
     *
     * @param force constant force
     */
    public ConstantForceBehavior(Vector3d force) {
        super();

        this.force = new Vector3d(force);
    }

    /**
     * Applies the behavior's force to the given particle.
     *
     * Results in the particle gaining velocity.
     *
     * @param particle the particle to apply the behavior to
     */
    @Override
    public void apply(Particle particle) {
        particle.translate(force);
    }
}
