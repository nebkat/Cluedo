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

package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Suspect;

/**
 * Suspect starting tile
 */
public class StartTile extends CorridorTile {
    private Suspect mStartingSuspect;

    public StartTile(int x, int y, Suspect startingSuspect) {
        super(x, y);
        mStartingSuspect = startingSuspect;
    }

    /**
     * Returns the suspect that starts on this tile.
     *
     * @return the suspect that starts on this tile.
     */
    public Suspect getStartingSuspect() {
        return mStartingSuspect;
    }
}
