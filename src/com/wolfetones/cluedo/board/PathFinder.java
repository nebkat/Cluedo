package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.OccupyableTile;
import com.wolfetones.cluedo.board.tiles.Tile;

import java.util.*;

public class PathFinder {
    public static int tileManhattanDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public static List<Tile> findQuickestPath(Tile start, Tile target, int moves) {
        if (tileManhattanDistance(start, target) > moves) {
            return null;
        }

        PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> {
            int fA = a.path.size() + tileManhattanDistance(a.tile, target);
            int fB = b.path.size() + tileManhattanDistance(b.tile, target);

            return fA - fB;
        });
        Map<Tile, Node> nodes = new HashMap<>();

        queue.add(new Node(start, Collections.singletonList(start)));

        while (!nodes.containsKey(target) && queue.size() > 0) {
            Node current = queue.poll();
            for (Tile t : current.tile.getNeighbours()) {
                if (!(t instanceof OccupyableTile)) continue;
                if (current.path.contains(t)) continue;
                if (current.path.size() >= moves && t != target) continue;

                if (nodes.containsKey(t) && nodes.get(t).path.size() <= current.path.size()) continue;

                List<Tile> path = new ArrayList<>(current.path);
                path.add(t);
                Node node = new Node(t, path);

                queue.add(node);
                nodes.put(t, node);
            }
        }

        return nodes.containsKey(target) ? nodes.get(target).path : null;
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
