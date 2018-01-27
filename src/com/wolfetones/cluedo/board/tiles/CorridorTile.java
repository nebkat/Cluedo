package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;

public class CorridorTile extends OccupyableTile {
    private Room mAdjacentRoom = null;

    public CorridorTile(int x, int y) {
        super(x, y);
    }

    public void setAdjacentRoom(Room room) {
        mAdjacentRoom = room;
    }

    public boolean hasDoor() {
        return mAdjacentRoom != null;
    }

    public Room getRoom() {
        return mAdjacentRoom;
    }
}
