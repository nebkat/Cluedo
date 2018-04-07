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

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Connection between two {@link Particle}s.
 *
 * Acts as a rigid stick or spring depending on the strength.
 */
public class Spring {
    private static final double DEFAULT_STRENGTH = 1;

    protected final Particle a;
    protected final Particle b;

    protected double length;

    protected double strength;

    /**
     * Constructs a rigid stick spring between the particles provided.
     * <p>
     * The length of the stick is set to the current distance between the particles.
     *
     * @param a particle a
     * @param b particle b
     */
    public Spring(Particle a, Particle b) {
        this(a, b, DEFAULT_STRENGTH);
    }

    /**
     * Constructs a spring between the particles provided with variable spring strength.
     * <p>
     * The length of the stick is set to the current distance between the particles.
     *
     * @param a particle a
     * @param b particle b
     * @param strength the strength of the spring
     */
    public Spring(Particle a, Particle b, double strength) {
        this.a = a;
        this.b = b;

        this.length = a.distance(b);

        this.strength = strength;
    }

    /**
     * Performs one step of physics simulation for spring.
     *
     * Moves the spring's particles equally towards/away from each other to attempt to satisfy target length.
     */
    public void update() {
        Vector3d delta = new Vector3d();
        delta.sub(b, a);

        double diff = (delta.length() - length) * strength / 2;

        delta.normalize();
        delta.scale(diff);

        if (!a.isFixed()) a.add(delta);
        if (!b.isFixed()) b.sub(delta);
    }

    /**
     * Returns the target distance between the spring's particles.
     *
     * @return the length of the spring
     */
    public double getLength() {
        return length;
    }

    /**
     * Sets the target distance between the spring's particles.
     *
     * @param length the length of the spring
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Returns the strength of the spring.
     *
     * @return the strength of the spring
     */
    public double getStrength() {
        return strength;
    }

    /**
     * Sets the strength of the spring.
     *
     * @param strength the strength of the spring
     */
    public void setStrength(double strength) {
        this.strength = strength;
    }

    /**
     * Projects the spring to the screen and draws it on the provided {@code Graphics} object.
     *
     * @param g graphics on which to draw the spring
     */
    public void draw(Graphics2D g) {
        Point2d pa = RenderUtils.projectToScreen(a);
        Point2d pb = RenderUtils.projectToScreen(b);

        g.setColor(Color.BLUE);

        Line2D.Double line = new Line2D.Double(pa.x, pa.y, pb.x, pb.y);
        g.draw(line);
    }
}
