package com.wolfetones.cluedo.card;

import com.wolfetones.cluedo.board.tiles.RoomTile;
import com.wolfetones.cluedo.board.tiles.Tile;

public class Weapon extends Token {
    public Weapon(int id, String name) {
        super(id, name);
    }

    @Override
    protected boolean validTile(Tile t) {
        return t instanceof RoomTile;
    }
}
