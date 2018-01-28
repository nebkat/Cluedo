package com.wolfetones.cluedo.board;

import com.wolfetones.cluedo.game.Player;

public interface PlayerOccupiable {
    void addPlayer(Player player);
    void removePlayer(Player player);
    boolean isOccupied();
}
