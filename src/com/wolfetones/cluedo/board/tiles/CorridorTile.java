package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.game.Location;

public class CorridorTile extends TokenOccupiableTile implements Location {
    public CorridorTile(int x, int y) {
        super(x, y);
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
        return t instanceof TokenOccupiableTile && mTokenTraversableNeighbours.contains(t);
    }

    public void addDoor(RoomTile t) {
        mTokenTraversableNeighbours.add(t);
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
