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
import java.util.List;
import java.util.Map;

public class Knowledge {
    private int mPlayerCount;
    private Map<Card, Map<Player, Status>> mCardPlayerStatuses = new HashMap<>();

    public Knowledge(List<Card> cards, List<Player> players) {
        mPlayerCount = players.size();

        for (Card card : cards) {
            Map<Player, Status> playerStatusMap = new HashMap<>();

            for (Player player : players) {
                playerStatusMap.put(player, new Status());
            }

            mCardPlayerStatuses.put(card, playerStatusMap);
        }
    }

    public Map<Card, Map<Player, Status>> get() {
        return mCardPlayerStatuses;
    }

    public void setHolding(Card card, Player player) {
        setValue(card, player, Value.Holding);
    }

    public void setValue(Card card, Player player, Value value) {
        Map<Player, Status> cardPlayerStatuses = mCardPlayerStatuses.get(card);
        Status playerStatus = cardPlayerStatuses.get(player);

        // Set value
        playerStatus.value = value;

        // Special cases
        switch (value) {
            case Holding:
                // Set all other entries to not holding
                cardPlayerStatuses.values().stream()
                        .filter(s -> s != playerStatus)
                        .forEach(s -> s.value = Value.NotHolding);

                break;
            case NotHolding:
                int notHoldingCount = (int) cardPlayerStatuses.values().stream()
                        .map(Status::getValue)
                        .filter(Value.NotHolding::equals)
                        .count();

                // If all but one player are not holding then set that player to holding
                if (notHoldingCount == mPlayerCount - 1) {
                    cardPlayerStatuses.values().stream()
                            .filter(s -> s.value != Value.NotHolding)
                            .findAny().ifPresent(s -> s.value = Value.Holding);
                }

                break;
        }
    }

    public void toggleHint(Card card, Player player, int hint) {
        mCardPlayerStatuses.get(card).get(player).toggleHint(hint);
    }

    public class Status {
        private Value value;
        private boolean[] hints = new boolean[5];

        public Value getValue() {
            return value;
        }

        public boolean[] getHints() {
            return hints;
        }

        public void toggleHint(int hint) {
            hints[hint] = !hints[hint];
        }
    }

    public enum Value {
        Holding, NotHolding, SuspectedHolding
    }
}
