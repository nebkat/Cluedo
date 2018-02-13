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

package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

import java.util.Arrays;
import java.util.List;

/**
 * Class representing a murder suggestion.
 *
 * Composed of a {@code Room}, {@code Suspect} and {@code Weapon}.
 */
public class Suggestion {
    public final Room room;
    public final Suspect suspect;
    public final Weapon weapon;

    /**
     * Suggestion constructor.
     *
     * @param r Room in which the murder was committed.
     * @param s Suspect that committed the murder.
     * @param w Weapon that was used in the murder.
     */
    public Suggestion(Room r, Suspect s, Weapon w) {
        room = r;
        suspect = s;
        weapon = w;
    }

    /**
     * Returns the {@code Room}, {@code Suspect} and {@code Weapon} cards of this suggestion in a list.
     *
     * @return The cards of the suggestion.
     */
    public List<Card> asList() {
        return Arrays.asList(room, suspect, weapon);
    }

    public String asHumanReadableString() {
        return suspect.getName() + " in the " + room.getName() + " with a " + weapon.getName();
    }

    /**
     * Custom comparator, returns {@code true} if {@code Suggestion}s are identical (same room/suspect/weapon).
     *
     * @param obj Object to compare to.
     * @return {@code true} if {@code Suggestion}s are identical.
     */
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
