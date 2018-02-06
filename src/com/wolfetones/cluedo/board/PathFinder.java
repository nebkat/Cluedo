package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.Tile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.game.Location;

import java.util.*;

/**
 * Useful path-finding functions
 */
public class PathFinder {
    /**
     * Returns the distance between two tiles along axes at right angles.
     *
     * @param a Tile A
     * @param b Tile B
     * @return The Manhattan distance between the two tiles
     */
    public static int tileManhattanDistance(Tile a, Tile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Finds the shortest path availaable between two tiles, taking into account rooms
     */
    public static List<Tile> findShortestPathAdvanced(Location fromTile, Location toTile, int maxMoves) {
        Room fromRoom = null;
        Room toRoom = null;

        // Get rooms if using room tiles
        if (fromTile instanceof Room) {
            fromRoom = (Room) fromTile;
        }
        if (toTile instanceof Room) {
            toRoom = (Room) toTile;
        }

        List<Tile> path;
        if (fromRoom != null && toRoom != null) { // Two
            // Loop through each possible combination of entrance corridors
            // to check whether the player can move between the two rooms
            for (CorridorTile fromRoomEntranceCorridor : fromRoom.getEntranceCorridors()) {
                for (CorridorTile toRoomEntranceCorridor : toRoom.getEntranceCorridors()) {
                    if ((path = PathFinder.findShortestPath(fromRoomEntranceCorridor.getDoorTile(),
                            toRoomEntranceCorridor.getDoorTile(),
                            maxMoves)) != null) {
                        return path;
                    }
                }
            }
        } else if (fromRoom == null && toRoom == null) { // No rooms
            return PathFinder.findShortestPath((CorridorTile) fromTile, (CorridorTile) toTile, maxMoves);
        } else { // One room
            Room loopRoom;
            Tile targetTile;
            if (fromRoom != null) {
                loopRoom = fromRoom;
                targetTile = (CorridorTile) toTile;
            } else {
                loopRoom = toRoom;
                targetTile = (CorridorTile) fromTile;
            }

            for (CorridorTile loopRoomEntranceCorridor : loopRoom.getEntranceCorridors()) {
                if ((path = PathFinder.findShortestPath(fromRoom != null ? loopRoomEntranceCorridor.getDoorTile() : targetTile,
                        fromRoom != null ? targetTile : loopRoomEntranceCorridor.getDoorTile(),
                        maxMoves)) != null) {
                    return path;
                }
            }
        }

        return null;
    }

    /**
     * Finds the shortest path availaable between two tiles, taking into account rooms
     */
    /*public static List<Tile> findShortestPathAdvanced(Tile fromTile, Tile toTile, int maxMoves) {
        Room fromRoom = null;
        Room toRoom = null;

        // Get rooms if using room tiles
        if (fromTile instanceof RoomTile) {
            fromRoom = ((RoomTile) fromTile).getRoom();
        }
        if (toTile instanceof RoomTile) {
            toRoom = ((RoomTile) toTile).getRoom();
        }

        List<Tile> path;
        if (fromRoom != null && toRoom != null) { // Two
            // Loop through each possible combination of entrance corridors
            // to check whether the player can move between the two rooms
            for (CorridorTile fromRoomEntranceCorridor : fromRoom.getEntranceCorridors()) {
                for (CorridorTile toRoomEntranceCorridor : toRoom.getEntranceCorridors()) {
                    if ((path = PathFinder.findShortestPath(fromRoomEntranceCorridor.getDoorTile(),
                            toRoomEntranceCorridor.getDoorTile(),
                            maxMoves)) != null) {
                        return path;
                    }
                }
            }
        } else if (fromRoom == null && toRoom == null) { // No rooms
            return PathFinder.findShortestPath(fromTile, toTile, maxMoves);
        } else { // One room
            Room loopRoom;
            Tile targetTile;
            if (fromRoom != null) {
                loopRoom = fromRoom;
                targetTile = toTile;
            } else {
                loopRoom = toRoom;
                targetTile = fromTile;
            }

            for (CorridorTile loopRoomEntranceCorridor : loopRoom.getEntranceCorridors()) {
                if ((path = PathFinder.findShortestPath(fromRoom != null ? loopRoomEntranceCorridor.getDoorTile() : targetTile,
                        fromRoom != null ? targetTile : loopRoomEntranceCorridor.getDoorTile(),
                        maxMoves)) != null) {
                    return path;
                }
            }
        }

        return null;
    }*/

    /**
     * Finds the shortest path available between two tiles.
     *
     * @param start The starting tile
     * @param target The target tile
     * @param maxMoves The maximum number of moves allowed to reach the target
     * @return A list of tiles containing the shortest path from {@code start} to {@code target}
     */
    public static List<Tile> findShortestPath(Tile start, Tile target, int maxMoves) {
        // If tiles are too distant by manhattan route a path is not possible
        if (tileManhattanDistance(start, target) > maxMoves) {
            return null;
        }

        // Priority queue to hold further explorable tiles
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> (double) expectedMovesForNode(node, target) + (double) node.turns * 0.01));

        // Map of tiles to nodes holding their shortest paths
        Map<Tile, Node> nodes = new HashMap<>();

        // If start tile is the target tile return immediately
        if (start == target) {
            return Collections.singletonList(start);
        }

        // Add the first
        queue.add(new Node(start, new ArrayList<>(), false, 0));

        while (queue.size() > 0) {
            Node currentNode = queue.poll();

            // If path has already exceeded max moves, don't check neighbours
            if (expectedMovesForNode(currentNode, target) > maxMoves) continue;

            // Append current tile to the path
            List<Tile> path = new ArrayList<>(currentNode.path);
            path.add(currentNode.tile);

            // Loop through neighbours
            for (TokenOccupiableTile neighbouringTile : currentNode.tile.getTokenTraversableNeighbours()) {
                // Only empty corridor tiles can be traversed
                if (neighbouringTile instanceof CorridorTile &&
                        neighbouringTile.isOccupied()) continue;

                // Don't loop through existing tiles
                if (currentNode.path.contains(neighbouringTile)) continue;

                // Check if moving to this node has caused a change of direction
                boolean vertical = currentNode.tile.getX() == neighbouringTile.getX();
                boolean turned = currentNode.vertical != vertical && currentNode.path.size() > 1;

                Node neighbouringNode;
                if (nodes.containsKey(neighbouringTile)) {
                    // Existing node
                    neighbouringNode = nodes.get(neighbouringTile);

                    // If tile already has node and path is shorter ignore long route
                    if (neighbouringNode.path.size() <= path.size() && neighbouringNode.turns <= currentNode.turns) {
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

    /**
     * Calculates the expected total moves required to reach the target tile from a node.
     *
     * Returns the sum of the current path to the tile along and
     * the Manhattan distance to the target tile, assuming the
     * optimal route with Manhattan distance is available.
     *
     * @param node The node from which to calculate
     * @param target The target tile
     * @return The expected total moves required to reach the {@code target} from the {@code node}
     */
    private static int expectedMovesForNode(Node node, Tile target) {
        return node.path.size() + tileManhattanDistance(node.tile, target);
    }

    /**
     * A node in the search for a path between two tiles
     */
    private static class Node {
        private Tile tile;
        private List<Tile> path;
        private boolean vertical;
        private int turns;

        /**
         * Constructs a new node
         *
         * @param t The tile
         * @param p The path taken to the tile from the starting tile
         * @param v Whether the last movement was vertical
         * @param d The total number of changes of direction
         */
        private Node(Tile t, List<Tile> p, boolean v, int d) {
            tile = t;
            path = p;
            vertical = v;
            turns = d;
        }
    }
}
