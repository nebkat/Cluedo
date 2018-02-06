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

    public Suspect getCharacter() {
        return mCharacter;
    }

    public String getName() {
        return mName;
    }

    public void addCard(Card card) {
        mCards.add(card);
        mKnowledge.add(card);
    }

    public void addKnowledge(Card card) {
        mKnowledge.add(card);
    }

    public boolean hasKnowledge(Card card) {
        return mKnowledge.contains(card);
    }

    public boolean hasAnySuggestionCards(Suggestion suggestion) {
        return !Collections.disjoint(mCards, suggestion.asList());
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
