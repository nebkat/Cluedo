package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

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
            "███░░░█░░░███ █████=█░░░█░░░█=█████ █░░░░░█░░░░░█" +
            "█1 1 1 1 1 1█ █=====█2 2 2 2█=====█ █3 3 3 3 3 3░" +
            "█           ███ █████       █████ ███           ░" +
            "░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░" +
            "░           █===█               █===█           █" +
            "░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░" +
            "█           █===█               █===█           ░" +
            "░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░" +
            "░           █===█               █===█▲█       ███" +
            "░1 1 1 1 1 1█===▲2 2 2 2 2 2 2 2▲=====█3 3 3 3█  " +
            "███         █===█               █=====███████████" +
            "  █1 1 1 1 1█===█2 2 2 2 2 2 2 2█==============@█" +
            "█████████▲███===█               █=============███" +
            "█===============█2 2 2 2 2 2 2 2█=============█  " +
            "███=============███▲█████████▲███===█████████████" +
            "  █=================================█5 5 5 5 5 5░" +
            "███████████=========================█           █" +
            "░4 4 4 4 4█=========================▲5 5 5 5 5 5░" +
            "░         ███████===███████████=====█           █" +
            "░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█5 5 5 5 5 5░" +
            "█               █===█         █=====█           █" +
            "░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█5 5 5 5 5 5░" +
            "░               █===█         █=====█           █" +
            "░4 4 4 4 4 4 4 4▲===█0 0 0 0 0█=====█5 5 5 5 5 5░" +
            "░               █===█         █=====█████████▲███" +
            "░4 4 4 4 4 4 4 4█===█0 0 0 0 0█===============█  " +
            "█               █===█         █=====█████▲█████  " +
            "░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█6 6 6 6 6█  " +
            "░               █===█         █===███         █░█" +
            "░4 4 4 4 4 4 4 4█===█0 0 0 0 0█===█6 6 6 6 6 6 6░" +
            "█████████████▲███===█         █===█             █" +
            "  █=================█0 0 0 0 0█===▲6 6 6 6 6 6 6░" +
            "███=================█████▲█████===█             █" +
            "█@================================█6 6 6 6 6 6 6░" +
            "███===============█████▲▲▲█████===███         █░█" +
            "  █===============█8 8 8 8 8 8█=====█6 6 6 6 6█  " +
            "█████████████▲█===█           █=====█████████████" +
            "█7 7 7 7 7 7 7█===█8 8 8 8 8 8█================@█" +
            "█             █===█           █===============███" +
            "░7 7 7 7 7 7 7█===█8 8 8 8 8 8▲===============█  " +
            "░             █===█           █===█▲█████████████" +
            "░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9█" +
            "█             █===█           █===█             █" +
            "░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9░" +
            "░             █===█           █===█             █" +
            "░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9░" +
            "█           ███=███           ███=███           ░" +
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
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                char c = BOARD_STRING.charAt(tileCoordinatesToBoardStringOffset(x, y));

                if (Character.isDigit(c)) {
                    int r = Integer.parseInt(Character.toString(c));

                    TILES[y][x] = new RoomTile(x, y, ROOMS[r]);
                } else {
                    switch (c) {
                        case TILE_EMPTY:
                            TILES[y][x] = new EmptyTile(x, y);
                            break;
                        case TILE_START:
                            TILES[y][x] = new StartTile(x, y, SUSPECTS[startTileSuspectIterator++]);
                            break;
                        case TILE_CORRIDOR:
                            TILES[y][x] = new CorridorTile(x, y);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown board tile found at [" + x + ", " + y + "]{" + (1 + (x * 2)) + ", " + (1 + (y * 2)) + "} = '" + c + "'");
                    }
                }
            }
        }

        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                if (TILES[y][x] == null) continue;

                // Set neighbouring tiles
                Tile left = x > 0 ? TILES[y][x - 1] : null;
                Tile up = y > 0 ? TILES[y - 1][x] : null;
                Tile right = x < (BOARD_WIDTH - 1) ? TILES[y][x + 1] : null;
                Tile down = y < (BOARD_HEIGHT - 1) ? TILES[y + 1][x] : null;

                TILES[y][x].setNeighbours(left, up, right, down);
            }
        }

        // Update doors
        for (int y = 0; y < BOARD_STRING_HEIGHT; y++) {
            boolean horizontal = y % 2 == 1;
            for (int x = horizontal ? 0 : 1; x < BOARD_STRING_WIDTH; x += 2) {
                if (BOARD_STRING.charAt(BoardModel.boardStringCoordinatesToOffset(x, y)) == TILE_DOOR) {
                    Tile a;
                    Tile b;
                    if (horizontal) {
                        a = TILES[y / 2][(x - 1) / 2];
                        b = TILES[y / 2][(x + 1) / 2];
                    } else {
                        a = TILES[(y - 1) / 2][x / 2];
                        b = TILES[(y + 1) / 2][x / 2];
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

                    corridorTile.setDoorRoom(roomTile.getRoom());
                    roomTile.getRoom().addEntranceCorridor(corridorTile);
                }
            }
        }
    }

    private static int[] tileCoordinatesToBoardStringCoordinates(int x, int y) {
        x = 1 + 2 * x;
        y = 1 + 2 * y;

        return new int[] {x, y};
    }

    private static int tileCoordinatesToBoardStringOffset(int x, int y) {
        return boardStringCoordinatesToOffset(1 + 2 * x, 1 + 2 * y);
    }

    private static int boardStringCoordinatesToOffset(int x, int y) {
        return x + y * BOARD_STRING_WIDTH;
    }

    public static char[] getTileBordersAndCorners(int x, int y) {
        int[] boardStringCoordinates = tileCoordinatesToBoardStringCoordinates(x, y);

        char[] bordersAndCorners = new char[8];
        x = boardStringCoordinates[0];
        y = boardStringCoordinates[1];

        bordersAndCorners[0] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y));
        bordersAndCorners[1] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y -1));
        bordersAndCorners[2] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x, y -1));
        bordersAndCorners[3] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y - 1));
        bordersAndCorners[4] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y));
        bordersAndCorners[5] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y + 1));
        bordersAndCorners[6] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x, y + 1));
        bordersAndCorners[7] = BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y + 1));

        return bordersAndCorners;
    }
}
