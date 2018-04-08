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

import javax.vecmath.Vector3d;

/**
 * Vector representing an axis.
 */
public class Axis extends Vector3d {
    /**
     * Constant representing the X axis (1, 0, 0).
     */
    public static final Axis X = new Axis(1, 0, 0);

    /**
     * Constant representing the Y axis (0, 1, 0).
     */
    public static final Axis Y = new Axis(0, 1, 0);

    /**
     * Constant representing the Z axis (0, 0, 1).
     */
    public static final Axis Z = new Axis(0, 0, 1);

    /**
     * Constructs a new axis in the specified direction.
     *
     * @param x distance x
     * @param y distance y
     * @param z distance z
     */
    private Axis(int x, int y, int z) {
        super(x, y, z);

        normalize();
    }
}
