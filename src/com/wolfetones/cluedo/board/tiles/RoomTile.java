package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;

public class RoomTile extends Tile {
    private Room mRoom;

    public RoomTile(int x, int y, Room room) {
        super(x, y);

        mRoom = room;
    }

    public Room getRoom() {
        return mRoom;
    }
}
