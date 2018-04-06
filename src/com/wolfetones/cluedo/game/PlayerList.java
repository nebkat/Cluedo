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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PlayerList extends ArrayList<Player> {
    public PlayerList() {
        super();
    }

    public Player getAfter(int index, Player after) {
        return get((index + indexOf(after) + 1) % size());
    }

    public Iterator<Player> iteratorStartingAfter(Player player) {
        int index = indexOf(player);

        return new Iterator<>() {
            int current = index;
            @Override
            public boolean hasNext() {
                return current != (index - 1 + size()) % size();
            }

            @Override
            public Player next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                current++;
                current %= size();

                return get(current);
            }
        };
    }
}
