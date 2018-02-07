package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;
import com.wolfetones.cluedo.game.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Room extends Card implements Location {
    private boolean mIsGuessRoom;
    private Room mPassageRoom;

    private List<RoomTile> mTiles = new ArrayList<>();
    private List<RoomTile> mEntranceCorridors = new ArrayList<>();

    private int mTileSumX = 0;
    private int mTileSumY = 0;

    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;

    public Room(int id, String name, boolean isGuessRoom) {
        super(id, name);

        mIsGuessRoom = isGuessRoom;
    }

    public void addTile(RoomTile tile) {
        if (mTiles.isEmpty()) {
            mMinX = tile.getX();
            mMaxX = tile.getX();
            mMinY = tile.getY();
            mMaxY = tile.getY();
        } else {
            mMinX = Math.min(mMinX, tile.getX());
            mMaxX = Math.max(mMaxX, tile.getX());
            mMinY = Math.min(mMinY, tile.getY());
            mMaxY = Math.max(mMaxY, tile.getY());
        }

        mTiles.add(tile);

        mTileSumX += tile.getX();
        mTileSumY += tile.getY();
    }

    public float getBoundingCenterX() {
        return ((float) mMaxX + mMinX) / 2f;
    }

    public float getBoundingCenterY() {
        return ((float) mMaxY + mMinY) / 2f;
    }

    public float getCenterX() {
        return ((float) mTileSumX) / mTiles.size() + 0.5f;
    }

    public float getCenterY() {
        return ((float) mTileSumY) / mTiles.size() + 0.5f;
    }

    private double tileDistanceFromCenter(RoomTile t) {
        float x = Math.abs(getBoundingCenterX() - t.getX());
        float y = Math.abs(getBoundingCenterY() - t.getY());

        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
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

    public RoomTile getNextUnoccupiedTile(Token token) {
        // If token is already in room return its current tile
        if (token.getTile() instanceof RoomTile && mTiles.contains(token.getTile())) {
            return (RoomTile) token.getTile();
        }

        return mTiles.stream()
                .filter(TokenOccupiableTile::isFree)
                .min(Comparator.comparingDouble(this::tileDistanceFromCenter))
                .orElse(null);
    }

    public List<RoomTile> getRoomTiles() {
        return mTiles;
    }

    public void addEntranceCorridor(RoomTile tile) {
        mEntranceCorridors.add(tile);
    }

    public List<RoomTile> getEntranceCorridors() {
        return mEntranceCorridors;
    }

    @Override
    public boolean isRoom() {
        return true;
    }

    @Override
    public Room asRoom() {
        return this;
    }
}
