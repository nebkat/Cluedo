package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;
import com.wolfetones.cluedo.game.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Room
 *
 * Holds information about the room's tiles and coordinates.
 */
public class Room extends Card implements Location {
    private boolean mIsGuessRoom;
    private Room mPassageRoom;

    private List<RoomTile> mTiles = new ArrayList<>();
    private List<RoomTile> mEntranceCorridors = new ArrayList<>();

    /**
     * Center coordinates calculations
     */
    private int mTileSumX = 0;
    private int mTileSumY = 0;

    /**
     * Bounding rectangle calculations
     */
    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;

    public Room(int id, String name, boolean isGuessRoom) {
        super(id, name);

        mIsGuessRoom = isGuessRoom;
    }

    /**
     * Adds a {@code RoomTile} to the room.
     *
     * Updates the bounding rectangle coordinates and center coordinate calculations.
     *
     * @param tile The tile to add.
     */
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

    /**
     * Returns the X coordinate of the center of the bounding rectangle of the room.
     *
     * @return The X coordinate of the center of the bounding rectangle of the room.
     */
    private float getBoundingCenterX() {
        return ((float) mMaxX + mMinX) / 2f;
    }

    /**
     * Returns the Y coordinate of the center of the bounding rectangle of the room.
     *
     * @return The Y coordinate of the center of the bounding rectangle of the room.
     */
    private float getBoundingCenterY() {
        return ((float) mMaxY + mMinY) / 2f;
    }

    /**
     * Returns the X coordinate of the center of the room (based on average tile coordinates).
     *
     * @return The X coordinate of the center of the room.
     */
    public float getCenterX() {
        return ((float) mTileSumX) / mTiles.size() + 0.5f;
    }

    /**
     * Returns the Y coordinate of the center of the room (based on average tile coordinates).
     *
     * @return The Y coordinate of the center of the room.
     */
    public float getCenterY() {
        return ((float) mTileSumY) / mTiles.size() + 0.5f;
    }

    /**
     * Returns the pythagorean distance of a tile from the center of the bounding rectangle of the room.
     *
     * @param t Tile from which to calculate distance to center.
     * @return The pythagorean distance of a tile from the center of the bounding rectangle of the room.
     */
    private double tileDistanceFromCenter(RoomTile t) {
        float x = Math.abs(getBoundingCenterX() - t.getX());
        float y = Math.abs(getBoundingCenterY() - t.getY());

        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * Returns {@code true} if this room is a guess room.
     *
     * A guess room is a room in which the final accusation is made.
     *
     * @return {@code true} if this room is a guess room.
     */
    public boolean isGuessRoom() {
        return mIsGuessRoom;
    }

    /**
     * Returns {@code true} if this room contains a passage to another room.
     *
     * @return {@code true} if this room contains a passage to another room.
     */
    public boolean hasPassage() {
        return mPassageRoom != null;
    }

    public void setPassageRoom(Room room) {
        mPassageRoom = room;
    }

    public Room getPassageRoom() {
        return mPassageRoom;
    }

    /**
     * Returns the tile closest to the center of the room that is not currently occupied.
     *
     * If the token being placed is already in a tile it is not moved and that tile is returned.
     *
     * @param token Token being placed.
     * @return The tile closest to the center of the room that is not currently occupied.
     */
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

    /**
     * A list of all of the tiles in the room.
     *
     * @return A list of all the tiles in the room.
     */
    public List<RoomTile> getRoomTiles() {
        return mTiles;
    }

    /**
     * Adds a {@code RoomTile} which has a door/leads to an adjacent {@code CorridorTile}.
     *
     * @param tile Tile which has a door.
     */
    public void addEntranceCorridor(RoomTile tile) {
        mEntranceCorridors.add(tile);
    }

    /**
     * Returns a list of all tiles in the room which have a door.
     *
     * @return A list of all tiles in the room which have a door.
     */
    public List<RoomTile> getEntranceCorridors() {
        return mEntranceCorridors;
    }
}
