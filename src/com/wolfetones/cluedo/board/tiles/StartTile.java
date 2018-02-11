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
