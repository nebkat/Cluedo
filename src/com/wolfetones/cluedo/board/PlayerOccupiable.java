package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.game.Player;

public interface PlayerOccupiable {
    void occupy(Player p);
    void leave(Player p);
    boolean isFullyOccupied();
}
