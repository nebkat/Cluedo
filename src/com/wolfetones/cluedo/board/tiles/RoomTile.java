package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Token;

public class RoomTile extends TokenOccupiableTile {
    private Room mRoom;
    private CorridorTile mDoorTile;

    public RoomTile(int x, int y, Room room) {
        super(x, y);

        mRoom = room;

        room.addTile(this);
    }

    public Room getRoom() {
        return mRoom;
    }

    public void setDoorTile(CorridorTile room) {
        mDoorTile = room;
    }

    public CorridorTile getDoorTile() {
        return mDoorTile;
    }

    public boolean hasDoor() {
        return mDoorTile != null;
    }
}
