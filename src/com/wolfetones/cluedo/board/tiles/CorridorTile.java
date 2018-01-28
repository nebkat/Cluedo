package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.board.PlayerOccupiable;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.game.Player;

public class CorridorTile extends Tile implements PlayerOccupiable {
    private Player mPlayer;

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

    @Override
    public void addPlayer(Player player) {
        mPlayer = player;
    }

    @Override
    public void removePlayer(Player player) {
        if (mPlayer == player) {
            mPlayer = null;
        }
    }

    @Override
    public boolean isOccupied() {
        return mPlayer != null;
    }
}
