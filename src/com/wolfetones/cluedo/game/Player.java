package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Suspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Player {
    private Suspect mCharacter;

    private String mName;

    private List<Card> mCards = new ArrayList<>();

    public Player(Suspect character, String name) {
        mCharacter = character;
        mName = name;
    }

    public Suspect getCharacter() {
        return mCharacter;
    }

    public String getName() {
        return mName;
    }

    public void addCard(Card card) {
        mCards.add(card);
    }

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
