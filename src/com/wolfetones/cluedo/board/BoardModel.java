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

package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;
import com.wolfetones.cluedo.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardModel {
    /**
     * Cards
     */
    private List<Room> mRooms = new ArrayList<>();
    private List<Suspect> mSuspects = new ArrayList<>();
    private List<Weapon> mWeapons = new ArrayList<>();

    /**
     * A 2D array containing the tiles on the board, in format {@code [y][x]}.
     */
    private Tile[][] mTiles = new Tile[Config.Board.HEIGHT][Config.Board.WIDTH];

    /**
     * Constructs a new {@code BoardModel} and initializes all cards and tiles.
     */
    public BoardModel() {
        // Initialize rooms
        for (int i = 0; i < Config.Cards.ROOMS.length; i++) {
            Config.Cards.Room configRoom = Config.Cards.ROOMS[i];

            mRooms.add(new Room(i, configRoom.name, configRoom.resource, configRoom.guess));
        }

        // Initialize suspects
        for (int i = 0; i < Config.Cards.SUSPECTS.length; i++) {
            Config.Cards.Suspect configSuspect = Config.Cards.SUSPECTS[i];

            mSuspects.add(new Suspect(i, configSuspect.name, configSuspect.resource, configSuspect.color));
        }

        // Initialize weapons
        for (int i = 0; i < Config.Cards.WEAPONS.length; i++) {
            Config.Cards.Weapon configWeapon = Config.Cards.WEAPONS[i];

            mWeapons.add(new Weapon(i, configWeapon.name, configWeapon.resource));
        }

        // Initialize tiles
        int startTileSuspectIterator = 0;
        for (int y = 0; y < Config.Board.HEIGHT; y++) {
            for (int x = 0; x < Config.Board.WIDTH; x++) {
                char c = Config.Board.BOARD_STRING.charAt(tileCoordinatesToBoardStringOffset(x, y));

                if (Character.isDigit(c)) {
                    // Room
                    int r = Integer.parseInt(Character.toString(c));

                    mTiles[y][x] = new RoomTile(x, y, mRooms.get(r));
                } else if (Config.Board.Tiles.PASSAGES.containsKey(c)) {
                    // Passage
                    int[] passage = Config.Board.Tiles.PASSAGES.get(c);

                    mTiles[y][x] = new PassageTile(x, y, mRooms.get(passage[0]), mRooms.get(passage[1]));
                } else {
                    // Other tile
                    switch (c) {
                        case Config.Board.Tiles.EMPTY:
                            mTiles[y][x] = new EmptyTile(x, y);
                            break;
                        case Config.Board.Tiles.START:
                            Suspect startingSuspect = mSuspects.get(Config.Board.Tiles.STARTS[startTileSuspectIterator++]);
                            mTiles[y][x] = new StartTile(x, y, startingSuspect);
                            startingSuspect.setLocation((CorridorTile) mTiles[y][x]);
                            break;
                        case Config.Board.Tiles.CORRIDOR:
                            mTiles[y][x] = new CorridorTile(x, y);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown board tile found at [" + x + ", " + y + "]{" + (1 + (x * 2)) + ", " + (1 + (y * 2)) + "} = '" + c + "'");
                    }
                }
            }
        }

        // Set neighbouring tiles
        for (int x = 0; x < Config.Board.WIDTH; x++) {
            for (int y = 0; y < Config.Board.HEIGHT; y++) {
                Tile left = x > 0 ? mTiles[y][x - 1] : null;
                Tile up = y > 0 ? mTiles[y - 1][x] : null;
                Tile right = x < (Config.Board.WIDTH - 1) ? mTiles[y][x + 1] : null;
                Tile down = y < (Config.Board.HEIGHT - 1) ? mTiles[y + 1][x] : null;

                mTiles[y][x].setNeighbours(left, up, right, down);
            }
        }

        // Update passages
        for (int[] passage : Config.Board.Tiles.PASSAGES.values()) {
            mRooms.get(passage[0]).setPassageRoom(mRooms.get(passage[1]));
        }

        // Update doors
        for (int y = 0; y < Config.Board.STRING_HEIGHT; y++) {
            // Checking horizontal or vertical doors depends on row
            boolean horizontal = y % 2 == 1;
            for (int x = horizontal ? 0 : 1; x < Config.Board.STRING_WIDTH; x += 2) {
                // Ignore all except doors
                if (Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x, y)) != Config.Board.Tiles.DOOR) continue;

                Tile a;
                Tile b;
                if (horizontal) {
                    a = mTiles[y / 2][(x - 1) / 2];
                    b = mTiles[y / 2][(x + 1) / 2];
                } else {
                    a = mTiles[(y - 1) / 2][x / 2];
                    b = mTiles[(y + 1) / 2][x / 2];
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

                roomTile.setDoorTile(corridorTile);
                corridorTile.addDoor(roomTile);
            }
        }

        // Place weapons in rooms
        List<Integer> weaponRooms = IntStream.range(1, mRooms.size())
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(weaponRooms);

        for (int i = 0; i < mWeapons.size(); i++) {
            mWeapons.get(i).setLocation(mRooms.get(weaponRooms.get(i)));
        }


        // Remove guess room from rooms list
        mRooms.remove(0);
    }

    /**
     * Returns the tile at the specified coordinates.
     *
     * @param x X coordinate of the tile.
     * @param y Y coordinate of the tile.
     * @return The tile at the specified coordinates.
     */
    public Tile getTile(int x, int y) {
        return mTiles[y][x];
    }

    /**
     * Returns a list containing all of the {@code Room} cards.
     *
     * @return a list containing all of the {@code Room} cards.
     */
    public List<Room> getRooms() {
        return new ArrayList<>(mRooms);
    }

    public Room getRoom(int id) {
        return mRooms.get(id);
    }

    public List<Suspect> getSuspects() {
        return new ArrayList<>(mSuspects);
    }

    public Suspect getSuspect(int id) {
        return mSuspects.get(id);
    }

    public List<Weapon> getWeapons() {
        return new ArrayList<>(mWeapons);
    }

    public Weapon getWeapon(int id) {
        return mWeapons.get(id);
    }

    private static int[] tileCoordinatesToBoardStringCoordinates(int x, int y) {
        x = 1 + 2 * x;
        y = 1 + 2 * y;

        return new int[] {x, y};
    }

    public static int tileCoordinatesToOffset(int x, int y) {
        return x + y * Config.Board.WIDTH;
    }

    private static int tileCoordinatesToBoardStringOffset(int x, int y) {
        return boardStringCoordinatesToOffset(1 + 2 * x, 1 + 2 * y);
    }

    public static int boardStringCoordinatesToOffset(int x, int y) {
        return x + y * Config.Board.STRING_WIDTH;
    }

    public static char[] getTileBordersAndCorners(int x, int y) {
        int[] boardStringCoordinates = tileCoordinatesToBoardStringCoordinates(x, y);

        char[] bordersAndCorners = new char[8];
        x = boardStringCoordinates[0];
        y = boardStringCoordinates[1];

        bordersAndCorners[0] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y));
        bordersAndCorners[1] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y -1));
        bordersAndCorners[2] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x, y -1));
        bordersAndCorners[3] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y - 1));
        bordersAndCorners[4] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y));
        bordersAndCorners[5] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x + 1, y + 1));
        bordersAndCorners[6] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x, y + 1));
        bordersAndCorners[7] = Config.Board.BOARD_STRING.charAt(boardStringCoordinatesToOffset(x - 1, y + 1));

        return bordersAndCorners;
    }
}
