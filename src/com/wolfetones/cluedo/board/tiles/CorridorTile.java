package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;

public class CorridorTile extends TokenOccupiableTile {
    private Room mDoorRoom = null;

    public CorridorTile(int x, int y) {
        super(x, y);
    }

    public void setDoorRoom(Room room) {
        mDoorRoom = room;
    }

    public boolean hasDoor() {
        return mDoorRoom != null;
    }

    public Room getRoom() {
        return mDoorRoom;
    }
}
