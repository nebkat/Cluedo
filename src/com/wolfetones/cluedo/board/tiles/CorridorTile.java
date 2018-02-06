package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.game.Location;

public class CorridorTile extends TokenOccupiableTile implements Location {
    private RoomTile mDoorTile = null;

    public CorridorTile(int x, int y) {
        super(x, y);
    }

    public void setDoorTile(RoomTile room) {
        mDoorTile = room;
        mTokenTraversableNeighbours.add(room);
    }

    public boolean hasDoor() {
        return mDoorTile != null;
    }

    public RoomTile getDoorTile() {
        return mDoorTile;
    }

    public boolean canMove() {
        return canMoveLeft() || canMoveUp() || canMoveRight() || canMoveDown();
    }

    public boolean canMoveLeft() {
        return canMoveToTile(mLeft);
    }

    public boolean canMoveUp() {
        return canMoveToTile(mUp);
    }

    public boolean canMoveRight() {
        return canMoveToTile(mRight);
    }

    public boolean canMoveDown() {
        return canMoveToTile(mDown);
    }

    private boolean canMoveToTile(Tile t) {
        return (t instanceof CorridorTile && !((CorridorTile) t).isOccupied()) ||
                t == mDoorTile;
    }

    @Override
    public boolean isRoom() {
        return false;
    }

    @Override
    public CorridorTile asTile() {
        return this;
    }
}
