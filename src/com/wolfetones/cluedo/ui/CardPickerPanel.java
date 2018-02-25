package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.config.Config;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CardPickerPanel<T extends Card> extends JPanel {
    private List<T> mCards;

    private T mResult;

    public CardPickerPanel(String title, List<T> cards) {
        super();

        mCards = cards;

        TitledBorder border = BorderFactory.createTitledBorder(null, title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)),
                Color.BLACK);

        setBorder(border);

        for (T c : mCards) {
            CardPickerCardComponent component = new CardPickerCardComponent<>(this, c);

            add(component);
        }
    }

    public void onCardComponentSelected(CardPickerCardComponent<T> cardComponent) {
        setResult(cardComponent.getCard());

        for (Component component : getComponents()) {
            if (component == cardComponent) continue;

            ((CardPickerCardComponent) component).setSelected(false);
        }
    }

    private void setResult(T c) {
        mResult = c;
    }

    public T getResult() {
        return mResult;
    }
}
