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
