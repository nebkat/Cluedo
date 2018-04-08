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
import com.wolfetones.cluedo.card.Suspect;

import java.util.*;
import java.util.stream.Collectors;

public class Player {
    private String mName;

    private Suspect mCharacter;

    private List<Card> mCards = new ArrayList<>();

    private Knowledge mKnowledge;

    public Player(Suspect character, String name) {
        mCharacter = character;
        mName = name;
    }

    public void initiateKnowledge(List<Card> cards, PlayerList players, List<Card> undistributedCards) {
        mKnowledge = new Knowledge(this, players, cards, undistributedCards);
    }

    public Knowledge getKnowledge() {
        return mKnowledge;
    }

    /**
     * Returns {@code true} if the player is holding the specified card.
     *
     * @param card the card to check
     * @return {@code true} if the player is holding the specified card
     */
    public boolean hasCard(Card card) {
        return mCards.contains(card);
    }

    /**
     * Adds a card to this list of cards that this player is holding.
     *
     * @param card the card to add
     */
    public void addCard(Card card) {
        mCards.add(card);
    }

    /**
     * Returns a list of cards that this player is holding.
     *
     * @return a list of cards that this player is holding
     */
    public List<Card> getCards() {
        return mCards;
    }

    /**
     * Returns the {@code Suspect} that this player is controlling.
     *
     * @return the {@code Suspect} that this player is controlling
     */
    public Suspect getCharacter() {
        return mCharacter;
    }

    public String getName() {
        return mName;
    }

    /**
     * Returns {@code true} if the player holds any of the cards used in the {@code suggestion}.
     *
     * @param suggestion the suggestion to check
     * @return {@code true} if the player holds any of the cards used in the {@code suggestion}
     */
    public boolean hasAnySuggestionCards(Suggestion suggestion) {
        return !Collections.disjoint(mCards, suggestion.asList());
    }

    /**
     * Returns a list of all {@code Card}s matching the cards in the {@code suggestion}.
     *
     * @param suggestion the suggestion to check
     * @return a list of all {@code Card}s matching the cards in the {@code suggestion}
     */
    public List<Card> matchingSuggestionCards(Suggestion suggestion) {
        List<Card> suggestionCards = suggestion.asList();
        if (Collections.disjoint(mCards, suggestionCards)) {
            return null;
        }

        return mCards
                .stream()
                .filter(suggestionCards::contains)
                .collect(Collectors.toList());
    }
}
