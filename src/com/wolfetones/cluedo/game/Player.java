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
    private Suspect mCharacter;

    private String mName;

    private List<Card> mCards = new ArrayList<>();

    private Set<Card> mKnowledge = new HashSet<>();

    public Player(Suspect character, String name) {
        mCharacter = character;
        mName = name;
    }

    /**
     * Returns the {@code Suspect} that this player is controlling.
     *
     * @return The {@code Suspect} that this player is controlling.
     */
    public Suspect getCharacter() {
        return mCharacter;
    }

    public String getName() {
        return mName;
    }

    /**
     * Adds a card to this player's cards and knowledge.
     *
     * @param card Card to add.
     */
    public void addCard(Card card) {
        mCards.add(card);
        mKnowledge.add(card);
    }

    /**
     * Adds the knowledge of a card being held by somebody else to this player's knowledge map.
     *
     * @param card Card to add to knowledge.
     */
    public void addKnowledge(Card card) {
        mKnowledge.add(card);
    }

    /**
     * Returns {@code true} if the player knows of a player having the {@code card}.
     *
     * @param card Card to check.
     * @return {@code true} if the player knows of a player having the {@code card}.
     */
    public boolean hasKnowledge(Card card) {
        return mKnowledge.contains(card);
    }

    /**
     * Returns {@code true} if the player holds any of the cards used in the {@code suggestion}.
     *
     * @param suggestion Suggestion to check.
     * @return {@code true} if the player holds any of the cards used in the {@code suggestion}.
     */
    public boolean hasAnySuggestionCards(Suggestion suggestion) {
        return !Collections.disjoint(mCards, suggestion.asList());
    }

    /**
     * Returns a list of all {@code Card}s matching the cards in the {@code suggestion}.
     *
     * @param suggestion Suggestion to check.
     * @return A list of all {@code Card}s matching the cards in the {@code suggestion}.
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
