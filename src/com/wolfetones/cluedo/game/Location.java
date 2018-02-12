package com.wolfetones.cluedo.game;

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
