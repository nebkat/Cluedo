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

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.card.Room;

/**
 * Interface representing possible locations for a token
 */
public interface Location {
    /**
     * Returns {@code true} if the {@code Location} is a {@code Room}.
     *
     * @return {@code true} if the {@code Location} is a {@code Room}.
     */
    default boolean isRoom() {
        return this instanceof Room;
    }

    /**
     * Returns the {@code Location} as a {@code Room}.
     *
     * @return The {@code Location} as a {@code Room}.
     * @throws IllegalStateException If the Location is not a Room.
     */
    default Room asRoom() {
        if (this instanceof Room) {
            return (Room) this;
        } else {
            throw new IllegalStateException("Location is not a Room");
        }
    }

    /**
     * Returns the {@code Location} as a {@code CorridorTile}.
     *
     * @return The {@code Location} as a {@code CorridorTile}.
     * @throws IllegalStateException If the Location is not a CorridorTile.
     */
    default CorridorTile asTile() {
        if (this instanceof CorridorTile) {
            return (CorridorTile) this;
        } else {
            throw new IllegalStateException("Location is not a CorridorTile");
        }
    }
}
