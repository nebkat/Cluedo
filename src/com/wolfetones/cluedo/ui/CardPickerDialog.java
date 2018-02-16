package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;

import javax.swing.*;
import java.util.List;

public class CardPickerDialog extends JDialog {

        private CardPickerDialog(boolean showName, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<Card> cards) {
            super();

            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            if (suspects != null) {
                add(new CardPickerPanel("Suspects", suspects));
            }

            if (weapons != null) {
                add(new CardPickerPanel("Weapons", weapons));
            }

            if (rooms != null) {
                add(new CardPickerPanel("Rooms", rooms));
            }

            if (cards != null) {
                add(new CardPickerPanel("Cards", cards));
            }

            setVisible(true);
            pack();
        }

        public static CardPickerDialog showPlayerPickerDialog(List<Suspect> suspects) {
            return new CardPickerDialog(true, suspects, null, null, null);
        }

        public static CardPickerDialog showAccusationPickerDialog(List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms) {
            return new CardPickerDialog(false, suspects, weapons, rooms, null);
        }

        public static CardPickerDialog showSuggestionPickerDialog(List<Suspect> suspects, List<Weapon> weapons) {
            return new CardPickerDialog(false, suspects, weapons, null, null);
        }

        public static CardPickerDialog showCardPickerDialog(List<Card> cards) {
            return new CardPickerDialog(false, null, null, null, cards);
        }
}
