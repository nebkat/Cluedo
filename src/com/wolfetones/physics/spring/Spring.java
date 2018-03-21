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

package com.wolfetones.physics.spring;

import com.wolfetones.physics.Particle;
import com.wolfetones.physics.RenderUtils;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.geom.Line2D;

public class Spring {
    private static final double DEFAULT_STRENGTH = 1;

    protected final Particle a;
    protected final Particle b;

    protected double length;

    protected double strength;

    public Spring(Particle a, Particle b) {
        this(a, b, DEFAULT_STRENGTH);
    }

    public Spring(Particle a, Particle b, double strength) {
        this.a = a;
        this.b = b;

        this.length = a.distance(b);

        this.strength = strength;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public void update() {
        Vector3d delta = new Vector3d();
        delta.sub(b, a);

        double diff = (delta.length() - length) * strength / 2;

        delta.normalize();
        delta.scale(diff);

        if (!a.isFixed()) a.add(delta);
        if (!b.isFixed()) b.sub(delta);
    }

    public void draw(Graphics2D g) {
        Point2d pa = RenderUtils.projectToScreen(a);
        Point2d pb = RenderUtils.projectToScreen(b);

        g.setColor(Color.BLUE);

        Line2D.Double line = new Line2D.Double(pa.x, pa.y, pb.x, pb.y);
        g.draw(line);
    }
}
