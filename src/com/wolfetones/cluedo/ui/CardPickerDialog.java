package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Player;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CardPickerDialog extends JDialog {

    private JTextField textField = new JTextField();
    private JButton button = new JButton("Ok");

    private CardPickerDialog(JFrame frame, boolean showName, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<? extends Card> cards) {
        super(frame, true);

        setUndecorated(true);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createDashedBorder(Color.BLACK, 4, 5, 5, true),
                        BorderFactory.createEmptyBorder(Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10))));

        if (showName) {
            add(textField);
            add(button);
        }

        button.addActionListener(e -> dispose());

        if (suspects != null) {
            add(new CardPickerPanel<>("Suspects", suspects));
        }

        if (weapons != null) {
            add(new CardPickerPanel<>("Weapons", weapons));
        }

        if (rooms != null) {
            add(new CardPickerPanel<>("Rooms", rooms));
        }

        if (cards != null) {
            add(new CardPickerPanel<>("Cards", cards));
        }

        pack();
        setLocation(frame.getX() + frame.getWidth() / 2 - getWidth() / 2, frame.getY() + frame.getHeight() / 2 - getHeight() / 2);
        setVisible(true);
    }

    public Player getPlayer() {
        String name;
        Card cardChoice;

        name = textField.getText();
        cardChoice = ( (CardPickerPanel) getContentPane().getComponent(0) ).getResult();
        Suspect suspect = (Suspect) cardChoice;

        Player player = new Player(suspect, name);

        return player;
    };

    public static Player showPlayerPickerDialog(JFrame frame, List<Suspect> suspects) {
        CardPickerDialog dialog =  new CardPickerDialog(frame, true, suspects, null, null, null);

        Player player = dialog.getPlayer();

        return player;
    }

    public static CardPickerDialog showAccusationPickerDialog(JFrame frame, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms) {
        return new CardPickerDialog(frame, false, suspects, weapons, rooms, null);
    }

    public static CardPickerDialog showSuggestionPickerDialog(JFrame frame, List<Suspect> suspects, List<Weapon> weapons) {
        return new CardPickerDialog(frame, false, suspects, weapons, null, null);
    }

    public static CardPickerDialog showCardPickerDialog(JFrame frame, List<? extends Card> cards) {
        return new CardPickerDialog(frame, false, null, null, null, cards);
    }
}
