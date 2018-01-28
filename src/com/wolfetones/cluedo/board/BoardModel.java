package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

import java.util.List;

public class BoardModel {

    /*
     * Rooms
     */
    public static final int GUESS_ROOM = 0;
    public static final int ROOM_COUNT = 10;

    public static final String[] ROOM_NAMES = {
            "Guess Room",
            "Kitchen",
            "Ball Room",
            "Conservatory",
            "Dining Room",
            "Billiard Room",
            "Library",
            "Lounge",
            "Hall",
            "Study"
    };

    /*
     * Suspects
     */
    public static final int SUSPECT_COUNT = 6;

    public static final String[] SUSPECT_NAMES = {
            "Miss Scarlett",
            "Colonel Mustard",
            "Mrs. White",
            "Reverend Green",
            "Mrs. Peacock",
            "Professor Plum"
    };

    public static final String[] SUSPECT_COLORS = {
            "red",
            "yellow",
            "white",
            "green",
            "blue",
            "purple"
    };

    /*
     * Weapons
     */
    public static final int WEAPON_COUNT = 6;

    public static final String[] WEAPON_NAMES = {
            "Candlestick",
            "Dagger",
            "Lead Pipe",
            "Revolver",
            "Rope",
            "Spanner"
    };

    /*
     * Board
     */
    public static final char TILE_EMPTY = ' ';
    public static final char TILE_WALL = '█';
    public static final char TILE_WINDOW = '░';
    public static final char TILE_DOOR = '▲';
    public static final char TILE_START = '@';
    public static final char TILE_CORRIDOR = '=';
    public static final char TILE_PASSAGE = 'P';

    public static final int BOARD_WIDTH = 24;
    public static final int BOARD_HEIGHT = 25;

    public static final int BOARD_STRING_WIDTH = 49;
    public static final int BOARD_STRING_HEIGHT = 51;
    public static final String BOARD_STRING =
            "                  ███       ███                  " +
            "                  █@█       █@█                  " +
            "███░░░█░░░███ █████ █░░░█░░░█ █████ █░░░░░█░░░░░█" +
            "█1 1 1 1 1 1█ █= = =█2 2 2 2█= = =█ █3 3 3 3 3 3░" +
            "█           ███ █████       █████ ███           ░" +
            "░1 1 1 1 1 1█= =█2 2 2 2 2 2 2 2█= =█3 3 3 3 3 3░" +
            "░           █   █               █   █           █" +
            "░1 1 1 1 1 1█= =█2 2 2 2 2 2 2 2█= =█3 3 3 3 3 3░" +
            "█           █   █               █   █           ░" +
            "░1 1 1 1 1 1█= =█2 2 2 2 2 2 2 2█= =█3 3 3 3 3 3░" +
            "░           █   █               █   █▲█       ███" +
            "░1 1 1 1 1 1█= =▲2 2 2 2 2 2 2 2▲= = =█3 3 3 3█  " +
            "███         █   █               █     ███████████" +
            "  █1 1 1 1 1█= =█2 2 2 2 2 2 2 2█= = = = = = = @█" +
            "█████████▲███   █               █             ███" +
            "█= = = = = = = =█2 2 2 2 2 2 2 2█= = = = = = =█  " +
            "███             ███▲█████████▲███   █████████████" +
            "  █= = = = = = = = = = = = = = = = =█5 5 5 5 5 5░" +
            "███████████                         █           █" +
            "░4 4 4 4 4█= = = = = = = = = = = = =▲5 5 5 5 5 5░" +
            "░         ███████   ███████████     █           █" +
            "░4 4 4 4 4 4 4 4█= =█0 0 0 0 0█= = =█5 5 5 5 5 5░" +
            "█               █   █         █     █           █" +
            "░4 4 4 4 4 4 4 4█= =█0 0 0 0 0█= = =█5 5 5 5 5 5░" +
            "░               █   █         █     █           █" +
            "░4 4 4 4 4 4 4 4▲= =█0 0 0 0 0█= = =█5 5 5 5 5 5░" +
            "░               █   █         █     █████████▲███" +
            "░4 4 4 4 4 4 4 4█= =█0 0 0 0 0█= = = = = = = =█  " +
            "█               █   █         █     █████▲█████  " +
            "░4 4 4 4 4 4 4 4█= =█0 0 0 0 0█= = =█6 6 6 6 6█  " +
            "░               █   █         █   ███         █░█" +
            "░4 4 4 4 4 4 4 4█= =█0 0 0 0 0█= =█6 6 6 6 6 6 6░" +
            "█████████████▲███   █         █   █             █" +
            "  █= = = = = = = = =█0 0 0 0 0█= =▲6 6 6 6 6 6 6░" +
            "███                 █████▲█████   █             █" +
            "█@ = = = = = = = = = = = = = = = =█6 6 6 6 6 6 6░" +
            "███               █████▲▲▲█████   ███         █░█" +
            "  █= = = = = = = =█8 8 8 8 8 8█= = =█6 6 6 6 6█  " +
            "█████████████▲█   █           █     █████████████" +
            "█7 7 7 7 7 7 7█= =█8 8 8 8 8 8█= = = = = = = = @█" +
            "█             █   █           █               ███" +
            "░7 7 7 7 7 7 7█= =█8 8 8 8 8 8▲= = = = = = = =█  " +
            "░             █   █           █   █▲█████████████" +
            "░7 7 7 7 7 7 7█= =█8 8 8 8 8 8█= =█9 9 9 9 9 9 9█" +
            "█             █   █           █   █             █" +
            "░7 7 7 7 7 7 7█= =█8 8 8 8 8 8█= =█9 9 9 9 9 9 9░" +
            "░             █   █           █   █             █" +
            "░7 7 7 7 7 7 7█= =█8 8 8 8 8 8█= =█9 9 9 9 9 9 9░" +
            "█           ███ ███           ███ ███           ░" +
            "█7 7 7 7 7 7█ █@█ █8 8 8 8 8 8█ █=█ █9 9 9 9 9 9░" +
            "█░░░█░█░█░░░█ ███ █████████████ ███ ███░░░█░█░░░█";

    /*
     * Tiles
     */
    public static final Tile[][] TILES = new Tile[BOARD_STRING_HEIGHT][BOARD_STRING_WIDTH];

    /*
     * Cards
     */
    public static final Room[] ROOMS = new Room[ROOM_COUNT];
    public static final Suspect[] SUSPECTS = new Suspect[SUSPECT_COUNT];
    public static final Weapon[] WEAPONS = new Weapon[WEAPON_COUNT];

    public static void initialize() {
        // Initialize cards
        for (int i = 0; i < ROOM_COUNT; i++) ROOMS[i] = new Room(i, ROOM_NAMES[i], i == GUESS_ROOM);
        for (int i = 0; i < SUSPECT_COUNT; i++) SUSPECTS[i] = new Suspect(i, SUSPECT_NAMES[i]);
        for (int i = 0; i < WEAPON_COUNT; i++) WEAPONS[i] = new Weapon(i, WEAPON_NAMES[i]);

        // Initialize tiles
        int startTileSuspectIterator = 0;
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                char c = BOARD_STRING.charAt((1 + (i * 2)) * BOARD_STRING_WIDTH + (1 + (j * 2)));

                if (Character.isDigit(c)) {
                    int r = Integer.parseInt(Character.toString(c));

                    TILES[i][j] = new RoomTile(i, j, ROOMS[r]);
                } else {
                    switch (c) {
                        case TILE_EMPTY:
                            TILES[i][j] = null;
                            break;
                        case TILE_START:
                            TILES[i][j] = new StartTile(i, j, SUSPECTS[startTileSuspectIterator++]);
                            break;
                        case TILE_CORRIDOR:
                            TILES[i][j] = new CorridorTile(i, j);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown board tile found at [" + i + ", " + j + "][" + (1 + (i * 2)) + ", " + (1 + (j * 2)) + " = " + ((1 + (i * 2)) * BOARD_STRING_WIDTH + (1 + (j * 2))) + "] '" + c + "' (" + ((int) c) + ")");
                    }
                }
            }
        }

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (TILES[i][j] == null) continue;

                // Set neighbouring tiles
                Tile left = j > 0 ? TILES[i][j - 1] : null;
                Tile up = i > 0 ? TILES[i - 1][j] : null;
                Tile right = j < (BOARD_WIDTH - 1) ? TILES[i][j + 1] : null;
                Tile down = i < (BOARD_HEIGHT - 1) ? TILES[i + 1][j] : null;

                TILES[i][j].setNeighbours(left, up, right, down);
            }
        }

        // Update doors
        for (int i = 0; i < BOARD_STRING_HEIGHT; i++) {
            boolean horizontal = i % 2 == 1;
            for (int j = horizontal ? 1 : 0; j < BOARD_STRING_WIDTH; j += 2) {
                if (BOARD_STRING.charAt(i * BOARD_STRING_WIDTH + j) == TILE_DOOR) {
                    Tile a;
                    Tile b;
                    if (horizontal) {
                        a = TILES[i / 2][(j - 1) / 2];
                        b = TILES[i / 2][(j + 1) / 2];
                    } else {
                        a = TILES[(i - 1) / 2][j / 2];
                        b = TILES[(i + 1) / 2][j / 2];
                    }

                    RoomTile roomTile;
                    CorridorTile corridorTile;

                    if (a instanceof RoomTile && b instanceof CorridorTile) {
                        roomTile = (RoomTile) a;
                        corridorTile = (CorridorTile) b;
                    } else if (a instanceof CorridorTile && b instanceof RoomTile) {
                        roomTile = (RoomTile) b;
                        corridorTile = (CorridorTile) a;
                    } else {
                        throw new IllegalArgumentException("Door does not connect corridor and room piece [" + a.getX() + ", " + a.getY() + "] and [" + b.getX() + ", " + b.getY() + "]");
                    }

                    corridorTile.setAdjacentRoom(roomTile.getRoom());
                    roomTile.getRoom().addAdjacentCorridor(corridorTile);
                }
            }
        }

        List<Tile> path = PathFinder.findQuickestPath(TILES[0][9], TILES[24][16], 100);
        if (path == null) return;
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (TILES[i][j] == null) {
                    System.out.print(" ");
                } else if (path.contains(TILES[i][j])) {
                    System.out.print(":");
                } else if (TILES[i][j] instanceof RoomTile) {
                    System.out.print("R");
                } else if (TILES[i][j] instanceof OccupyableTile) {
                    System.out.print("=");
                }
            }
            System.out.print("\n");
        }
        for (Tile t : path) {
            System.out.println(t.getX() + ", " + t.getY());
        }

    }
}
