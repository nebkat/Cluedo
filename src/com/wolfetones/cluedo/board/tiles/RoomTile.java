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

package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Token;

/**
 * Tile within a {@link Room}
 */
public class RoomTile extends TokenOccupiableTile {
    /**
     * The {@code Room} that this tile is in.
     */
    private Room mRoom;

    /**
     * The {@link CorridorTile} this {@code RoomTile} leads to through a door
     */
    private CorridorTile mDoorTile;

    public RoomTile(int x, int y, Room room) {
        super(x, y);

        mRoom = room;
        mRoom.addTile(this);
    }

    /**
     * The {@code Room} that this tile is in.
     * @return the {@code Room} that this tile is in.
     */
    public Room getRoom() {
        return mRoom;
    }

    /**
     * Sets the {@code CorridorTile} that this {@code RoomTile} leads to through a door.
     *
     * Also defines itself as a corridor in the room using {@link Room#addEntranceCorridor}.
     *
     * @param doorTile the {@code CorridorTile} that this {@code RoomTile} leads to through a door.
     */
    public void setDoorTile(CorridorTile doorTile) {
        mDoorTile = doorTile;
        mTokenTraversableNeighbours.add(doorTile);

        mRoom.addEntranceCorridor(this);
    }

    /**
     * Gets the {@code CorridorTile} that this {@code RoomTile} leads to through a door.
     *
     * @return the {@code CorridorTile} that this {@code RoomTile} leads to through a door.
     */
    public CorridorTile getDoorTile() {
        return mDoorTile;
    }
}
