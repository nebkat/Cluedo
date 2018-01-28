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
        PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> {
            double af = expectedMovesForNode(a, target) + a.turns * 0.01;
            double bf = expectedMovesForNode(b, target) + b.turns * 0.01;

            return Double.compare(bf, af);
        });

        // Map of tiles to nodes holding their shortest paths
        Map<Tile, Node> nodes = new HashMap<>();

        // Add the first
        queue.add(new Node(start, Collections.singletonList(start), false, 0));

        int minTurns = -1;

        while (queue.size() > 0) {
            Node currentNode = queue.poll();

            // If path has already exceeded max moves, don't check neighbours
            if (expectedMovesForNode(currentNode, target) > maxMoves) continue;

            // Append current tile to the path
            List<Tile> path = new ArrayList<>(currentNode.path);
            path.add(currentNode.tile);

            // Loop through neighbours
            for (Tile neighbouringTile : currentNode.tile.getNeighbours()) {
                // Only occupiable tiles can be traversed
                if (!(neighbouringTile instanceof OccupiableTile)) continue;

                // Don't loop through existing tiles
                if (currentNode.path.contains(neighbouringTile)) continue;

                // Check if moving to this node has caused a change of direction
                boolean vertical = currentNode.tile.getX() == neighbouringTile.getX();
                boolean turned = currentNode.vertical != vertical;

                Node neighbouringNode;
                if (nodes.containsKey(neighbouringTile)) {
                    // Existing node
                    neighbouringNode = nodes.get(neighbouringTile);

                    // If tile already has node and path is shorter ignore long route
                    if (neighbouringNode.path.size() <= currentNode.path.size()) {
                        continue;
                    }

                    // Old path was longer, replace with new path
                    neighbouringNode.path = path;
                    neighbouringNode.vertical = vertical;
                    neighbouringNode.turns = currentNode.turns + (turned ? 1 : 0);
                } else {
                    // New node
                    neighbouringNode = new Node(neighbouringTile, path, vertical, currentNode.turns + (turned ? 1 : 0));

                    nodes.put(neighbouringTile, neighbouringNode);
                }

                // Attempting to find target
                if (neighbouringTile != target) {
                    queue.add(neighbouringNode);
                } else if (minTurns < 0 || neighbouringNode.turns < minTurns){
                    // Max moves is now the shortest path length
                    maxMoves = path.size();
                    minTurns = neighbouringNode.turns;

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
        private boolean vertical;
        private int turns;

        private Node(Tile t, List<Tile> p, boolean v, int d) {
            tile = t;
            path = p;
            vertical = v;
            turns = d;
        }
    }
}
