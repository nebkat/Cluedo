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
    private CardPickerDialog mCardPickerDialog;
    private List<CardPickerCardComponent> mComponents = new ArrayList<>();

    private T mResult;

    public CardPickerPanel(CardPickerDialog dialog, String title, List<T> cards) {
        super();

        mCardPickerDialog = dialog;

        TitledBorder border = BorderFactory.createTitledBorder(null, title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)),
                Color.BLACK);

        setBorder(border);

        for (T c : cards) {
            CardPickerCardComponent component = new CardPickerCardComponent<>(this, c);
            mComponents.add(component);

            add(component);
        }

        if (cards.size() == 1) {
            mResult = cards.get(0);
            mComponents.get(0).setSelected(true);
        }

        if (cards.size() < 3) {
            Insets borderInsets = border.getBorderInsets(this);
            Dimension cardSize = mComponents.get(0).getPreferredSize();
            int width = cardSize.width * 3 + 2 * ((FlowLayout) getLayout()).getHgap() + borderInsets.left + borderInsets.right;
            Dimension preferredSize = getPreferredSize();
            preferredSize.width = width;
            setPreferredSize(preferredSize);
        }

    }

    public void onCardComponentSelected(CardPickerCardComponent<T> cardComponent) {
        mResult = cardComponent.getCard();
        mCardPickerDialog.updateConfirmButton();

        for (CardPickerCardComponent component : mComponents) {
            if (component == cardComponent) continue;

            component.setSelected(false);
        }
    }

    public T getResult() {
        return mResult;
    }
}
