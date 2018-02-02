package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Suggestion {
    public final Room room;
    public final Suspect suspect;
    public final Weapon weapon;

    public Suggestion(Room r, Suspect s, Weapon w) {
        room = r;
        suspect = s;
        weapon = w;
    }

    public List<Card> asList() {
        return Arrays.asList(room, suspect, weapon);
    }

    public String asSuggestionString() {
        return suspect.getName() + " in the " + room.getName() + " with a " + weapon.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Suggestion)) {
            return false;
        }

        Suggestion s = (Suggestion) obj;

        return room == s.room &&
                suspect == s.suspect &&
                weapon == s.weapon;
    }
}
