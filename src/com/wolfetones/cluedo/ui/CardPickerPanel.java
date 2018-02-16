package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Card;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class CardPickerPanel extends JPanel {
    private List<? extends Card> mCards;

    public CardPickerPanel(String title, List<? extends Card> cards) {
        super();

        mCards = cards;

        TitledBorder border = BorderFactory.createTitledBorder(null, title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(Font.SANS_SERIF, Font.PLAIN, 32),
                Color.BLACK);

        setBorder(border);

        for (Card c : mCards) {
            JLabel label = new JLabel(new ImageIcon(c.getCardImage()));

            add(label);

        }
    }
}
