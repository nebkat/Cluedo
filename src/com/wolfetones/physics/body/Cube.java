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
import com.wolfetones.physics.RenderUtils;
import com.wolfetones.physics.VectorUtils;

import javax.vecmath.Point3d;

public class Cube extends RigidBody {
    private static final int[] FACE_PARTICLE_INDEX = {0, 1, 3, 2};

    protected Particle[][] faces = new Particle[6][4];

    public Cube(Point3d center, double sideLength) {
        super();

        double s = sideLength / 2.0;

        for (int xO = 0; xO <= 1; xO++) {
            for (int yO = 0; yO <= 1; yO++) {
                for (int zO = 0; zO <= 1; zO++) {
                    int xM = 2 * xO - 1;
                    int yM = 2 * yO - 1;
                    int zM = 2 * zO - 1;

                    Particle p = new Particle(center.x + xM * s, center.y + yM * s, center.z + zM * s);

                    particles.add(p);

                    faces[zO][faceParticleIndex(zO, xO, yO)] = p;
                    faces[2 + yO][faceParticleIndex(yO, zO, xO)] = p;
                    faces[4 + xO][faceParticleIndex(xO, yO, zO)] = p;
                }
            }
        }

        setupSticks();
    }

    private static int faceParticleIndex(int front, int a, int b) {
        int result = FACE_PARTICLE_INDEX[a + 2 * b];

        return front == 1 ? result : 3 - result;
    }

    protected static boolean isFrontFace(Particle[] face) {
        double v1x = RenderUtils.screenDistance(face[0].x, face[0].z) - RenderUtils.screenDistance(face[1].x, face[1].z);
        double v1y = RenderUtils.screenDistance(face[0].y, face[0].z) - RenderUtils.screenDistance(face[1].y, face[1].z);
        double v2x = RenderUtils.screenDistance(face[2].x, face[2].z) - RenderUtils.screenDistance(face[1].x, face[1].z);
        double v2y = RenderUtils.screenDistance(face[2].y, face[2].z) - RenderUtils.screenDistance(face[1].y, face[1].z);
        double cross = v1x * v2y - v1y * v2x;

        return cross < 0;
    }
}
