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

import java.util.*;

/**
 * Player knowledge of who is holding which cards.
 */
public class Knowledge {
    private Map<Card, Map<Player, Status>> mCardPlayerStatuses = new HashMap<>();

    public Knowledge(Player player, PlayerList players, List<Card> cards, List<Card> undistributedCards) {
        // Fill card player map
        for (Card card : cards) {
            Map<Player, Status> playerStatusMap = new HashMap<>();
            Iterator<Player> iterator = players.iteratorStartingAfter(player);
            while (iterator.hasNext()) {
            Player p = iterator.next();
                Status status = new Status();
                playerStatusMap.put(p, status);

                if (undistributedCards.contains(card)) {
                    status.value = Value.Undistributed;
                    status.fixed = true;
                } else if (player.hasCard(card)) {
                    status.value = Value.Self;
                    status.fixed = true;
                }
            }

            mCardPlayerStatuses.put(card, playerStatusMap);
        }
    }

    /**
     * Returns the player status map for the specified card.
     *
     * @param card the card of interest
     * @return the player status map for the specified card
     */
    public Map<Player, Status> get(Card card) {
        return mCardPlayerStatuses.get(card);
    }

    /**
     * Sets the specified player to be holding or not holding the specified card.
     *
     * To be used by {@link Game} only, for cards that have been shown or are known to be held by someone else.
     *
     * As the status is certain, the value is fixed to prevent accidental modification by the user.
     *
     * @param card the card that is held/not held
     * @param player the player that is holding/not holding the card
     * @param holding whether the player is holding the card
     *
     * @see Knowledge#setValue(Card, Player, Value, boolean)
     */
    void setHolding(Card card, Player player, boolean holding) {
        setValue(card, player, holding ? Value.Holding : Value.NotHolding, true);
    }

    /**
     * Sets the status of the specified player for the specified card, without fixing it's value.
     *
     * @param card the card for which the status is being set
     * @param player the player for which the status is being set
     * @param value the value being set
     *
     * @see Knowledge#setValue(Card, Player, Value, boolean)
     */
    public void setValue(Card card, Player player, Value value) {
        setValue(card, player, value, false);
    }

    /**
     * Sets the status of the specified player for the specified card, optionally fixing its value.
     *
     * If the value is fixed, it can no longer be modified.
     *
     * @param card the card for which the status is being set
     * @param player the player for which the status is being set
     * @param value the value being set
     * @param fixed whether to fix the value being set
     */
    private void setValue(Card card, Player player, Value value, boolean fixed) {
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

            // When all of a player's cards have been found, the other cards can be set to not held
            if (fixed) {
                int playerCards = mCardPlayerStatuses.size() / (cardPlayerStatuses.size() + 1);

                boolean foundAllCards = mCardPlayerStatuses.keySet().stream()
                        .map(c -> mCardPlayerStatuses.get(c).get(player))
                        .filter(Status::isFixed)
                        .map(Status::getValue)
                        .filter(Value.Holding::equals)
                        .count() == playerCards;

                if (foundAllCards) {
                    for (Card c : mCardPlayerStatuses.keySet()) {
                        setHolding(c, player, false);
                    }
                }
            }
        }
    }

    /**
     * Toggles one of the 4 available hints on a specified card for a specified player.
     *
     * @param card
     * @param player
     * @param hint
     */
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

        public boolean isFixed() {
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
        Holding, NotHolding, SuspectedHolding, SuspectedNotHolding, Undistributed, Self
    }
}
