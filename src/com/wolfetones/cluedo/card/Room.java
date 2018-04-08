/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.PassageTile;
import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.Location;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.ui.Animator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Room
 *
 * Holds information about the room's tiles and coordinates.
 */
public class Room extends Card implements Location {
    /**
     * How to lay out tokens in center of room depending on the number of tokens
     */
    private static final int[][] TOKEN_LAYOUTS = {
            {1},
            {2},
            {2, 1},
            {2, 2},
            {3, 2},
            {3, 3},
            {2, 3, 2},
            {3, 3, 2},
            {3, 3, 3},
            {3, 4, 3},
            {4, 4, 3},
            {4, 4, 4}
    };

    private List<Token> mTokens = new ArrayList<>();

    private boolean mIsGuessRoom;
    private Room mPassageRoom;
    private PassageTile mPassageTile;

    private List<RoomTile> mTiles = new ArrayList<>();
    private List<RoomTile> mEntranceCorridors = new ArrayList<>();

    private JComponent mLabel;

    /**
     * Center coordinates calculations
     */
    private int mTileSumX = 0;
    private int mTileSumY = 0;

    public Room(String name, String[] searchNames, String cardImage, boolean isGuessRoom) {
        super(name, searchNames, cardImage);

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
        mTiles.add(tile);

        mTileSumX += tile.getX();
        mTileSumY += tile.getY();

        if (tile instanceof PassageTile) {
            mPassageTile = (PassageTile) tile;
        }
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

    public PassageTile getPassageTile() {
        return mPassageTile;
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

    /**
     * Adds the token to the list of tokens currently in the room.
     *
     * @param token Token being added to the room.
     */
    public void addToken(Token token) {
        mTokens.add(token);

        updateTokenLocations();
    }

    /**
     * Removes the token from the list of tokens currently in the room.
     *
     * @param token Token being removed from the room.
     */
    public void removeToken(Token token) {
        mTokens.remove(token);

        updateTokenLocations();
    }

    /**
     * Updates the coordinates of tokens in the room to be distributed centrally
     */
    private void updateTokenLocations() {
        int count = mTokens.size();

        float centerX = getCenterX() - 0.5f;
        float centerY = getCenterY() - 0.5f;

        // No tokens to update
        if (count == 0) {
            // Move label back to center
            if (mLabel != null) {
                Animator.getInstance().animate(mLabel)
                        .translate(mLabel.getX(), (int) (centerY * mLabel.getHeight()))
                        .setDuration(200)
                        .start();
            }

            return;
        }

        // Choose the appropriate token layout for the number of tokens
        int[] layout = TOKEN_LAYOUTS[count - 1];

        int tokenIndex = 0;
        int rows = layout.length;

        // Move label above top row
        if (mLabel != null) {
            float targetY = centerY - (float) (rows + 1) / 2;

            Animator.getInstance().animate(mLabel)
                    .translate(mLabel.getX(), (int) (targetY * mLabel.getHeight()))
                    .setDuration(200)
                    .start();
        }

        // Move tokens
        float relativeRow = (float) (1 - rows) / 2f;
        for (int i = 0; i < rows; i++, relativeRow++) {
            int columns = layout[i];
            float relativeColumn = (float) (1 - columns) / 2f;
            for (int j = 0; j < columns; j++, relativeColumn++) {
                mTokens.get(tokenIndex++).setCoordinates(relativeColumn + centerX, relativeRow + centerY);
            }
        }
    }

    /**
     * Sets the label component associated with this room.
     *
     * Used to move the label when tokens enter the room.
     *
     * @param label the label component associated with this room.
     */
    public void setLabel(JComponent label) {
        mLabel = label;

        updateTokenLocations();
    }

    @Override
    protected String getCardImageSuffix() {
        return "room";
    }
}
