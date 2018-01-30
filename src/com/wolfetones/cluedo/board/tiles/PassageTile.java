package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;

public class PassageTile extends RoomTile {
    private Room mPassageRoom;

    public PassageTile(int x, int y, Room room, Room passageRoom) {
        super(x, y, room);

        mPassageRoom = passageRoom;
    }

    public Room getPassageRoom() {
        return mPassageRoom;
    }
}
