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
import com.wolfetones.physics.Spring;

/**
 * An abstract body of particles connected by rigid springs.
 */
public abstract class RigidBody extends Body {
    /**
     * Creates a rigid spring between each particle in the body.
     */
    protected void setupSticks() {
        for (Particle a : particles) {
            for (Particle b : particles) {
                if (a == b) continue;

                springs.add(new Spring(a, b));
            }
        }
    }
}
