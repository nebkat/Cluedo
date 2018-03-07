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

package com.wolfetones.physics;

import com.wolfetones.physics.behavior.Behavior;
import com.wolfetones.physics.constraint.Constraint;
import com.wolfetones.physics.body.Body;
import com.wolfetones.physics.spring.Spring;

import java.util.ArrayList;
import java.util.List;

public class VerletPhysics {
    private List<Particle> particles = new ArrayList<>();
    private List<Spring> springs = new ArrayList<>();
    private List<Body> bodies = new ArrayList<>();

    private int constraintSteps;

    private List<Behavior> behaviors = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();

    public VerletPhysics(int constraintSteps) {
        this.constraintSteps = constraintSteps;
    }

    public void addParticle(Particle p) {
        particles.add(p);
        behaviors.forEach(p::addBehavior);
        constraints.forEach(p::addConstraint);
    }

    public void addStick(Spring s) {
        springs.add(s);
    }

    public void addBody(Body g) {
        bodies.add(g);
        g.getParticles().forEach(this::addParticle);
        g.getSprings().forEach(this::addStick);
    }

    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);

        particles.forEach((p) -> p.addBehavior(behavior));
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);

        particles.forEach((p) -> p.removeBehavior(behavior));
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);

        particles.forEach((p) -> p.addConstraint(constraint));
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);

        particles.forEach((p) -> p.removeConstraint(constraint));
    }

    public void update() {
        for (Particle particle : particles) {
            particle.update();
        }

        for (int i = 0; i < constraintSteps; i++) {
            for (Particle particle : particles) {
                particle.applyConstraints();
            }

            for (Spring spring : springs) {
                spring.update();
            }
        }
    }
}
