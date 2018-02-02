package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Token;

public class RoomTile extends TokenOccupiableTile {
    private Room mRoom;

    public RoomTile(int x, int y, Room room) {
        super(x, y);

        mRoom = room;

        room.addTile(this);
    }

    public Room getRoom() {
        return mRoom;
    }

    @Override
    public void setToken(Token token) {
        if (mToken != null) {
            mRoom.removeToken(mToken);
        }

        super.setToken(token);

        if (token != null) {
            mRoom.addToken(token);
        }
    }
}
