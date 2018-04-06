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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Knowledge {
    private Map<Card, Map<Player, Status>> mCardPlayerStatuses = new HashMap<>();

    public Knowledge(Player player, PlayerList players, List<Card> cards, List<Card> playerCards, List<Card> undistributedCards) {
        for (Card card : cards) {
            Map<Player, Status> playerStatusMap = new HashMap<>();
            Iterator<Player> iterator = players.iteratorStartingAfter(player);
            Player p;
            while ((p = iterator.next()) != player) {
                Status status = new Status();
                playerStatusMap.put(p, status);

                if (undistributedCards.contains(card)) {
                    status.value = Value.Undistributed;
                    status.fixed = true;
                } else if (playerCards.contains(card)) {
                    status.value = Value.Self;
                    status.fixed = true;
                }
            }

            mCardPlayerStatuses.put(card, playerStatusMap);
        }
    }

    public Map<Card, Map<Player, Status>> get() {
        return mCardPlayerStatuses;
    }

    public void setHolding(Card card, Player player, boolean holding) {
        setValue(card, player, holding ? Value.Holding : Value.NotHolding, true);
    }

    public void setValue(Card card, Player player, Value value, boolean fixed) {
        Map<Player, Status> cardPlayerStatuses = mCardPlayerStatuses.get(card);
        Status playerStatus = cardPlayerStatuses.get(player);

        // Don't modify fixed values
        if (playerStatus.fixed) {
            return;
        }

        // Set value
        playerStatus.value = value;
        playerStatus.fixed = fixed;

        // Special case
        if (value == Value.Holding) {
            // Set all other entries to not holding
            for (Player p : cardPlayerStatuses.keySet()) {
                if (p == player) continue;
                setValue(card, p, Value.NotHolding, fixed);
            }
        }
    }

    public void toggleHint(Card card, Player player, int hint) {
        mCardPlayerStatuses.get(card).get(player).toggleHint(hint);
    }

    public class Status {
        private Value value;
        private boolean fixed = false;
        private boolean[] hints = new boolean[4];

        public Value getValue() {
            return value;
        }

        public boolean getFixed() {
            return fixed;
        }

        public boolean[] getHints() {
            return hints;
        }

        public void toggleHint(int hint) {
            hints[hint] = !hints[hint];
        }
    }

    public enum Value {
        Holding, NotHolding, SuspectedHolding, Undistributed, Self
    }
}
