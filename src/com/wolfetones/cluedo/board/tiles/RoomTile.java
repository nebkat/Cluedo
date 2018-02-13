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
