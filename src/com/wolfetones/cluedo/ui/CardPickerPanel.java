package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Card;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CardPickerPanel extends JPanel {
    private List<? extends Card> mCards;

    public CardPickerPanel(List<? extends Card> cards) {
        super();

        mCards = cards;

        for (Card c : mCards) {
            JLabel label = new JLabel(new ImageIcon(c.getCardImage()));

            add(label);

        }
    }
}
