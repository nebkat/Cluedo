package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.CorridorTile;

import java.util.ArrayList;
import java.util.List;

public class Room extends Card {
    private boolean mIsGuessRoom;
    private Room mPassageRoom;

    private List<CorridorTile> mAdjacentCorridors = new ArrayList<>();

    private float mCenterX;
    private float mCenterY;

    private int mTileCount;

    public Room(int id, String name, boolean isGuessRoom) {
        super(id, name);

        mIsGuessRoom = isGuessRoom;
    }

    public void addTileCoordinatesToCenterCalculation(int x, int y) {
        float totalX = mCenterX * (float) mTileCount + x;
        float totalY = mCenterY * (float) mTileCount + y;

        mTileCount++;

        mCenterX = totalX / (float) mTileCount;
        mCenterY = totalY / (float) mTileCount;
    }

    public float getCenterX() {
        return mCenterX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public boolean isGuessRoom() {
        return mIsGuessRoom;
    }

    public boolean hasPassage() {
        return mPassageRoom != null;
    }

    public void setPassageRoom(Room room) {
        mPassageRoom = room;
    }

    public Room getPassageRoom() {
        return mPassageRoom;
    }

    public void addAdjacentCorridor(CorridorTile tile) {
        mAdjacentCorridors.add(tile);
    }
}
