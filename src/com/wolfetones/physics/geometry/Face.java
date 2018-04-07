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

import com.wolfetones.physics.RenderUtils;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Collection of vertices representing a geometric face. Provides various utility methods to simplify face calculations.
 */
public class Face implements Iterable<Point3d> {
    private Point3d[] vertices;

    /**
     * Constructs a face with the given amount of vertices.
     *
     * @param vertexCount the number of vertices in the face
     */
    public Face(int vertexCount) {
        if (vertexCount < 3) {
            throw new IllegalArgumentException("Face must have at least 3 vertices");
        }
        vertices = new Point3d[vertexCount];
    }

    /**
     * Creates an array of faces with the given vertex count.
     *
     * @param faceCount the number of faces
     * @param vertexCount the number of vertices in each face
     * @return an array of faces with the given vertex count
     */
    public static Face[] initiateArray(int faceCount, int vertexCount) {
        Face[] faces = new Face[faceCount];
        for (int i = 0; i < faceCount; i++) {
            faces[i] = new Face(vertexCount);
        }

        return faces;
    }

    /**
     * Sets the vertex at the given index to the vertex provided.
     *
     * @param index index of the vertex to set
     * @param vertex the vertex to be stored at the given index
     */
    public void setVertex(int index, Point3d vertex) {
        vertices[index] = vertex;
    }

    /**
     * Gets the vertex at the given index.
     *
     * @param index the index of the vertex to set
     * @return the vertex at the given index
     */
    public Point3d getVertex(int index) {
        return vertices[index];
    }

    /**
     * Gets the vector between two vertices of the face.
     *
     * @param from the starting vertex
     * @param to the ending vertex
     * @return the vector between the two specified vertices of the face
     */
    public Vector3d getVertexDelta(int from, int to) {
        if (from >= vertices.length || to >= vertices.length || from < 0 || to < 0) {
            throw new IndexOutOfBoundsException("Invalid indices provided");
        }

        Vector3d delta = new Vector3d();
        delta.sub(vertices[to], vertices[from]);

        return delta;
    }

    /**
     * Gets the normal vector of the face.
     *
     * @return the normal vector of the face
     */
    public Vector3d getNormal() {
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();

        v1.sub(vertices[1], vertices[0]);
        v2.sub(vertices[2], vertices[1]);

        Vector3d normal = new Vector3d();
        normal.cross(v1, v2);

        return normal;
    }

    /**
     * Returns {@code true} if the front of the face is visible when projected on to the screen.
     *
     * @return {@code true} if the front of the face is visible when projected on to the screen.
     */
    public boolean isFrontFace() {
        Point2d p0 = RenderUtils.projectToScreen(vertices[0]);
        Point2d p1 = RenderUtils.projectToScreen(vertices[1]);
        Point2d p2 = RenderUtils.projectToScreen(vertices[2]);

        double v1x = p0.x - p1.x;
        double v1y = p0.y - p1.y;
        double v2x = p2.x - p1.x;
        double v2y = p2.y - p1.y;
        double cross = v1x * v2y - v1y * v2x;

        return cross < 0;
    }

    /**
     * Returns an iterator over the vertices of this face.
     *
     * @return an iterator over the vertices of this face.
     */
    @Override
    public Iterator<Point3d> iterator() {
        return Arrays.asList(vertices).iterator();
    }
}
