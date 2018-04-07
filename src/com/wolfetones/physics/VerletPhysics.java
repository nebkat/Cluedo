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

import java.util.ArrayList;
import java.util.List;

/**
 * 3D particle physics engine using Verlet integration.
 */
public class VerletPhysics {
    /**
     * List of particles in the simulation.
     */
    private List<Particle> particles = new ArrayList<>();

    /**
     * List of springs in the simulation.
     */
    private List<Spring> springs = new ArrayList<>();

    /**
     * Number of constraint iterations per update.
     */
    private int constraintSteps;

    /**
     * System behaviors.
     */
    private List<Behavior> behaviors = new ArrayList<>();

    /**
     * System constraints.
     */
    private List<Constraint> constraints = new ArrayList<>();

    /**
     * Initializes a new physics system.
     *
     * @param constraintSteps the number of constraint iterations per update
     */
    public VerletPhysics(int constraintSteps) {
        this.constraintSteps = constraintSteps;
    }

    /**
     * Adds a particle to the simulation.
     *
     * @param p the particle to add
     */
    public void addParticle(Particle p) {
        particles.add(p);
        behaviors.forEach(p::addBehavior);
        constraints.forEach(p::addConstraint);
    }

    /**
     * Adds a spring to the simulation.
     *
     * @param s the spring to add
     */
    public void addSpring(Spring s) {
        springs.add(s);
    }

    /**
     * Adds a body to the simulation.
     *
     * All of the bodies particles and springs are added to the system.
     *
     * @param g the body to add
     */
    public void addBody(Body g) {
        g.getParticles().forEach(this::addParticle);
        g.getSprings().forEach(this::addSpring);
    }

    /**
     * Adds a global behavior to the simulation.
     *
     * All particles in the system will be affected by the behavior.
     *
     * @param behavior the behavior to add
     */
    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);

        particles.forEach((p) -> p.addBehavior(behavior));
    }

    /**
     * Removes a global behavior from the simulation.
     *
     * Particles in the system will no longer be affected by the behavior.
     *
     * @param behavior the behavior to remove
     */
    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);

        particles.forEach((p) -> p.removeBehavior(behavior));
    }

    /**
     * Adds a global constraint to the simulation.
     *
     * All particles in the system will be affected by the constraint.
     *
     * @param constraint the constraint to add
     */
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);

        particles.forEach((p) -> p.addConstraint(constraint));
    }

    /**
     * Removes a global constraint from the simulation.
     *
     * Particles in the system will no longer be affected by the constraint.
     *
     * @param constraint the constraint to remove
     */
    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);

        particles.forEach((p) -> p.removeConstraint(constraint));
    }

    /**
     * Performs one step of the physics simulation.
     *
     * All particles positions are updated and then constraint iterations are applied.
     */
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
