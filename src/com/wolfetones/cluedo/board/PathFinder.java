package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.OccupiableTile;
import com.wolfetones.cluedo.board.tiles.Tile;

import java.util.*;

public class PathFinder {
    public static int tileManhattanDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private static int expectedMovesForNode(Node node, Tile target) {
        return node.path.size() + tileManhattanDistance(node.tile, target);
    }

    public static List<Tile> findQuickestPath(Tile start, Tile target, int maxMoves) {
        // If tiles are too distant by manhattan route a path is not possible
        if (tileManhattanDistance(start, target) > maxMoves) {
            return null;
        }

        // Priority queue to hold further explorable tiles
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(a -> expectedMovesForNode(a, target)));

        // Map of tiles to nodes holding their shortest paths
        Map<Tile, Node> nodes = new HashMap<>();

        // Add the first
        queue.add(new Node(start, Collections.singletonList(start)));

        while (queue.size() > 0) {
            Node current = queue.poll();

            // If path has already exceeded max moves, don't check neighbours
            if (expectedMovesForNode(current, target) >= maxMoves) continue;

            // Append current tile to the path
            List<Tile> path = new ArrayList<>(current.path);
            path.add(current.tile);

            // Loop through neighbours
            for (Tile t : current.tile.getNeighbours()) {
                // Only occupiable tiles can be traversed
                if (!(t instanceof OccupiableTile)) continue;

                // Don't loop through existing tiles
                if (current.path.contains(t)) continue;

                Node node;
                if (nodes.containsKey(t)) {
                    // Existing node
                    node = nodes.get(t);

                    // If tile already has node and path is shorter ignore long route
                    if (node.path.size() <= current.path.size()) {
                        continue;
                    }

                    // Old path was longer, replace with new path
                    node.path = path;
                } else {
                    // New node
                    node = new Node(t, path);

                    nodes.put(t, node);
                }

                // Attempting to find target
                if (t != target) {
                    queue.add(node);
                } else {
                    // Max moves is now the shortest path length
                    maxMoves = path.size();

                    // If target has been found no need to check neighbours further
                    break;
                }
            }
        }

        // If path to target was found return
        if (nodes.containsKey(target)) {
            List<Tile> path = nodes.get(target).path;
            path.add(target);
            return path;
        }

        return null;
    }

    private static class Node {
        private Tile tile;
        private List<Tile> path;

        private Node(Tile t, List<Tile> p) {
            tile = t;
            path = p;
        }
    }
}
