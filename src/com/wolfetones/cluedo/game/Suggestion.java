package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

public class Suggestion {
    public final Room room;
    public final Suspect suspect;
    public final Weapon weapon;

    public Suggestion(Room r, Suspect s, Weapon w) {
        room = r;
        suspect = s;
        weapon = w;
    }
}
