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
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Particle extends Point3d {
    public Point3d previousPosition = new Point3d();
    private Vector3d velocity = new Vector3d();

    private Point3d initialPosition = new Point3d();

    private List<Behavior> behaviors = new ArrayList<>();
    private List<Constraint> constraints = new ArrayList<>();

    private boolean fixed = false;
    
    public Particle(double x, double y, double z) {
        this(new Point3d(x, y, z));
    }

    public Particle(Point3d position) {
        this.set(position);
        this.previousPosition.set(position);
        this.initialPosition.set(position);
    }
    
    public void reset() {
        set(initialPosition);
        previousPosition.set(initialPosition);
    }
    
    public void update() {
        if (fixed) return;

        velocity.sub(this, previousPosition);
        previousPosition.set(this);

        this.add(velocity);

        for (Behavior behavior : behaviors) {
            behavior.apply(this);
        }
    }

    public void applyConstraints() {
        for (Constraint constraint : constraints) {
            constraint.apply(this);
        }
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    public void translate(Vector3d translate) {
        translate(translate, false);
    }

    public void translate(Vector3d translate, boolean updatePrevious) {
        this.add(translate);
        if (updatePrevious) {
            previousPosition.add(translate);
        }
    }

    public void transform(Matrix3d transform, Point3d transformOrigin) {
        transform(transform, false, transformOrigin);
    }

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

    public void draw(Graphics2D g) {
        double px = RenderUtils.screenDistance(x, z);
        double py = RenderUtils.screenDistance(y, z);

        g.setColor(Color.RED);
        g.fillOval((int) (px - 3), (int) (py - 3), 6, 6);
    }
}
