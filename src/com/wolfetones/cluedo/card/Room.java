package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.TokenOccupiableTile;

import java.util.ArrayList;
import java.util.List;

public class Room extends Card {
    private boolean mIsGuessRoom;
    private Room mPassageRoom;

    private List<Token> mTokens = new ArrayList<>();

    private List<RoomTile> mTiles = new ArrayList<>();
    private List<CorridorTile> mEntranceCorridors = new ArrayList<>();

    private float mTileSumX = 0;
    private float mTileSumY = 0;

    public Room(int id, String name, boolean isGuessRoom) {
        super(id, name);

        mIsGuessRoom = isGuessRoom;
    }

    public void addTile(RoomTile tile) {
        mTiles.add(tile);

        mTileSumX += tile.getX();
        mTileSumY += tile.getY();
    }

    public float getCenterX() {
        return mTileSumX / mTiles.size() + 0.5f;
    }

    public float getCenterY() {
        return mTileSumY / mTiles.size() + 0.5f;
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

    public void addToken(Token token) {
        mTokens.add(token);
    }

    public void removeToken(Token token) {
        mTokens.remove(token);
    }

    public RoomTile getNextUnoccupiedTile() {
        return mTiles.stream()
                .filter(TokenOccupiableTile::isFree)
                .findFirst()
                .orElse(null);
    }

    public List<RoomTile> getRoomTiles() {
        return mTiles;
    }

    public void addEntranceCorridor(CorridorTile tile) {
        mEntranceCorridors.add(tile);
    }

    public List<CorridorTile> getEntranceCorridors() {
        return mEntranceCorridors;
    }
}
