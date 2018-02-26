package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.Util;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.Suggestion;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class CardPickerDialog extends JDialog {

    private JTextField mNameTextField;
    private CardPickerPanel<Suspect> mSuspectCardPicker;
    private CardPickerPanel<Weapon> mWeaponCardPicker;
    private CardPickerPanel<Room> mRoomCardPicker;
    private CardPickerPanel<? extends Card> mUserCardPicker;

    private JButton mConfirmButton;
    private JButton mCancelButton;

    private boolean mSuccessful;

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

        mConfirmButton = new JButton("Confirm");
        mCancelButton = new JButton("No More Players");

        mConfirmButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)));
        mCancelButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)));

        buttonPanel.add(mCancelButton);
        buttonPanel.add(mConfirmButton);
        mConfirmButton.addActionListener(e -> {
            mSuccessful = true;
            dispose();
        });
        mCancelButton.addActionListener(e -> {
            mSuccessful = false;
            dispose();
        });

        if (showName) {
            mNameTextField = new JTextField();

            JPanel textFieldHolder = new JPanel();
            textFieldHolder.setLayout(new BorderLayout());
            textFieldHolder.setBorder(BorderFactory.createTitledBorder(null, "Player Name",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)),
                    Color.BLACK));
            textFieldHolder.add(mNameTextField);

            mNameTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(20)));

            add(textFieldHolder);

            // At least two players must be added
            if (suspects.size() > 4) {
                mCancelButton.setVisible(false);
            }
        } else {
            mCancelButton.setVisible(false);
        }

        if (suspects != null) {
            mSuspectCardPicker = new CardPickerPanel<>(this, "Suspects", suspects);

            add(mSuspectCardPicker);
        }

        if (weapons != null) {
            mWeaponCardPicker = new CardPickerPanel<>(this, "Weapons", weapons);

            add(mWeaponCardPicker);
        }

        if (rooms != null) {
            mRoomCardPicker = new CardPickerPanel<>(this, "Rooms", rooms);

            add(mRoomCardPicker);
        }

        if (cards != null) {
            mUserCardPicker = new CardPickerPanel<>(this, "Cards", cards);

            add(mUserCardPicker);
        }

        add(buttonPanel);

        updateConfirmButton();

        pack();
        setLocation(frame.getX() + frame.getWidth() / 2 - getWidth() / 2, frame.getY() + frame.getHeight() / 2 - getHeight() / 2);
        setVisible(true);
    }

    void updateConfirmButton() {
        mConfirmButton.setEnabled((mSuspectCardPicker == null || mSuspectCardPicker.getResult() != null) &&
                (mWeaponCardPicker == null || mWeaponCardPicker.getResult() != null) &&
                (mRoomCardPicker == null || mRoomCardPicker.getResult() != null) &&
                (mUserCardPicker == null || mUserCardPicker.getResult() != null)
        );
    }

    private Player getPlayerResult() {
        if (!mSuccessful) return null;

        String name = mNameTextField.getText();
        Suspect suspect = mSuspectCardPicker.getResult();

        if (name.isEmpty()) {
            name = suspect.getName();
        }

        return new Player(suspect, name);
    }

    public static Player showPlayerPickerDialog(JFrame frame, List<Suspect> suspects) {
        CardPickerDialog dialog =  new CardPickerDialog(frame, true, suspects, null, null, null);

        return dialog.getPlayerResult();
    }

    private Suggestion getAccusationResult() {
        if (!mSuccessful) return null;

        Suspect suspect = mSuspectCardPicker.getResult();
        Weapon weapon = mWeaponCardPicker.getResult();
        Room room = mRoomCardPicker.getResult();

        return new Suggestion(room, suspect, weapon);
    }

    public static Suggestion showAccusationPickerDialog(JFrame frame, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms) {
        CardPickerDialog dialog = new CardPickerDialog(frame, false, suspects, weapons, rooms, null);

        return dialog.getAccusationResult();
    }

    private Suggestion getSuggestionResult(Room room) {
        if (!mSuccessful) return null;

        Suspect suspect = mSuspectCardPicker.getResult();
        Weapon weapon = mWeaponCardPicker.getResult();

        return new Suggestion(room, suspect, weapon);
    }

    public static Suggestion showSuggestionPickerDialog(JFrame frame, List<Suspect> suspects, List<Weapon> weapons, Room room) {
        CardPickerDialog dialog = new CardPickerDialog(frame, false, suspects, weapons, null, null);

        return dialog.getSuggestionResult(room);
    }

    private Card getCardResult() {
        if (!mSuccessful) return null;

        return mUserCardPicker.getResult();
    }

    public static Card showCardPickerDialog(JFrame frame, List<? extends Card> cards) {
        CardPickerDialog dialog = new CardPickerDialog(frame, false, null, null, null, cards);

        return dialog.getCardResult();
    }
}
