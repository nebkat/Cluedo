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

import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D particle in Verlet integration physics engine
 */
public class Particle extends Point3d {
    public final Point3d previousPosition = new Point3d();
    private Vector3d velocity = new Vector3d();

    private Point3d initialPosition = new Point3d();

    private List<Behavior> behaviors = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();

    private boolean fixed = false;

    /**
     * Initiates a particle at the coordinates specified.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public Particle(double x, double y, double z) {
        this(new Point3d(x, y, z));
    }

    /**
     * Initiates a particle at the position specified.
     *
     * @param position particle position
     */
    public Particle(Point3d position) {
        this.set(position);
        this.previousPosition.set(position);
        this.initialPosition.set(position);
    }

    /**
     * Places the particle back at its initial position with 0 velocity.
     */
    public void reset() {
        set(initialPosition);
        previousPosition.set(initialPosition);
    }

    /**
     * Perform one step of physics simulation for particle.
     *
     * Moves the particle's position by the current velocity and applies all behaviors attached to particle.
     */
    public void update() {
        // If the particle is fixed it does not move
        if (fixed) return;

        // Calculate velocity and set previous position to current
        velocity.sub(this, previousPosition);
        previousPosition.set(this);

        // Add velocity to current position
        this.add(velocity);

        // Apply all behaviors
        for (Behavior behavior : behaviors) {
            behavior.apply(this);
        }
    }

    /**
     * Applies all constraints attached to particle
     */
    public void applyConstraints() {
        for (Constraint constraint : constraints) {
            constraint.apply(this);
        }
    }

    /**
     * Returns {@code true} if the particle is fixed in space, prevented from moving.
     *
     * @return {@code true} if the particle is fixed in space
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Sets whether the particle is fixed in space, prevented from moving.
     *
     * @param fixed whether the particle is fixed in space
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Attaches a behavior to this particle.
     *
     * Behaviors are applied to the particle on every step of the physics simulation.
     *
     * @param behavior behavior to add
     */
    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    /**
     * Removes a behavior from this particle.
     *
     * @param behavior behavior to remove
     */
    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    /**
     * Attaches a constraint to this particle.
     * <p>
     * Constraints are applied to the particle a number of times on every step of the physics simulation,
     * depending on its {@link VerletPhysics#constraintSteps}.
     *
     * @param constraint constraint to add
     */
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    /**
     * Removes a constraint from this particle.
     *
     * @param constraint constraint to remove
     */
    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    /**
     * Applies a translation to the particle without updating the previous position.
     *
     * @see Particle#translate(Vector3d, boolean)
     *
     * @param translate the direction in which to translate
     */
    public void translate(Vector3d translate) {
        translate(translate, false);
    }

    /**
     * Applies a translation to the particle, optionally updating the previous position of the particle.
     * <p>
     * If the previous position is not updated, additional velocity is generated. This can be used to apply forces
     * to particles.
     *
     * @param translate the direction in which to translate
     * @param updatePrevious whether to update the previous position
     */
    public void translate(Vector3d translate, boolean updatePrevious) {
        this.add(translate);
        if (updatePrevious) {
            previousPosition.add(translate);
        }
    }

    /**
     * Applies a transformation to the particle, without updating the previous position.
     *
     * @see Particle#transform(Matrix3d, boolean, Point3d)
     *
     * @param transform the transformation to apply
     * @param transformOrigin the point around which to perform the transformation
     */
    public void transform(Matrix3d transform, Point3d transformOrigin) {
        transform(transform, false, transformOrigin);
    }

    /**
     * Applies a transformation to the particle, optionally updating the previous position of the particle.
     * <p>
     * If the previous position is not updated, additional velocity is generated. This can be used to apply forces
     * to particles.
     * <p>
     * The transformation origin can be used to apply transformations relative to a body or particles, for example
     * the rotation of a cube around its center.
     *
     * @param transform the transformation to apply
     * @param updatePrevious whether to update the previous position
     * @param transformOrigin the point around which to perform the transformation
     */
    public void transform(Matrix3d transform, boolean updatePrevious, Point3d transformOrigin) {
        Point3d originTranslatedPosition = new Point3d(this);
        originTranslatedPosition.sub(transformOrigin);
        transform.transform(originTranslatedPosition);
        originTranslatedPosition.add(transformOrigin);

        this.set(originTranslatedPosition);

        if (updatePrevious) {
            Point3d originTranslatedPreviousPosition = new Point3d(previousPosition);
            originTranslatedPreviousPosition.sub(transformOrigin);
            transform.transform(originTranslatedPreviousPosition);
            originTranslatedPreviousPosition.add(transformOrigin);

            previousPosition.set(originTranslatedPreviousPosition);
        }
    }

    /**
     * Projects the particle to the screen and draws it on the provided {@code Graphics} object.
     *
     * @param g graphics on which to draw the particle
     */
    public void draw(Graphics2D g) {
        Point2d p = RenderUtils.projectToScreen(this);

        g.setColor(Color.RED);
        g.fillOval((int) (p.x - 3), (int) (p.y - 3), 6, 6);
    }
}
