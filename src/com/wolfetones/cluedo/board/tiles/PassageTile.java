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

/**
 * Room passage tile
 */
public class PassageTile extends RoomTile {
    private Room mPassageRoom;

    public PassageTile(int x, int y, Room room, Room passageRoom) {
        super(x, y, room);

        mPassageRoom = passageRoom;
    }

    /**
     * The {@code Room} that this passage leads to.
     *
     * @return the {@code Room} that this passage leads to.
     */
    public Room getPassageRoom() {
        return mPassageRoom;
    }
}
