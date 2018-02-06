package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.tiles.CorridorTile;
import com.wolfetones.cluedo.card.Room;

public interface Location {
    boolean isRoom();

    default Room asRoom() {
        return null;
    }
    default CorridorTile asTile() {
        return null;
    }
}
