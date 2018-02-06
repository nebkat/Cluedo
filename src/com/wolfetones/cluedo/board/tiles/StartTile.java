package com.wolfetones.cluedo.board.tiles;

import com.wolfetones.cluedo.card.Suspect;

public class StartTile extends CorridorTile {
    private Suspect mStartingSuspect;

    public StartTile(int x, int y, Suspect startingSuspect) {
        super(x, y);
        startingSuspect.setLocation(this);
        mStartingSuspect = startingSuspect;
    }

    public Suspect getStartingSuspect() {
        return mStartingSuspect;
    }
}
