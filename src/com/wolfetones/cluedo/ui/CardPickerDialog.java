package com.wolfetones.cluedo.ui;

import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.util.Util;
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CardPickerDialog extends JDialog {

    private JTextField mNameTextField;
    private CardPickerPanel<Suspect> mSuspectCardPicker;
    private CardPickerPanel<Weapon> mWeaponCardPicker;
    private CardPickerPanel<Room> mRoomCardPicker;
    private CardPickerPanel<? extends Card> mUserCardPicker;

    private JButton mConfirmButton;

    private boolean mSuccessful;

    private CardPickerDialog(JFrame frame, JComponent board, String title, boolean showName, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms, List<? extends Card> cards) {
        super(frame, true);

        setUndecorated(frame != null);

        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createDashedBorder(Color.WHITE, 4, 5, 5, true),
                        BorderFactory.createEmptyBorder(Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10),
                                Config.screenRelativeSize(10))));
        panel.setOpaque(false);

        if (title != null) {
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
            titlePanel.setOpaque(false);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(32)));
            titleLabel.setForeground(Color.WHITE);

            titlePanel.add(titleLabel);
            panel.add(titlePanel);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.setOpaque(false);

        mConfirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        mConfirmButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)));
        cancelButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)));

        buttonPanel.add(cancelButton);
        buttonPanel.add(mConfirmButton);
        mConfirmButton.addActionListener(this::onButtonClicked);
        cancelButton.addActionListener(this::onButtonClicked);

        if (showName) {
            mNameTextField = new JTextField();

            JPanel textFieldHolder = new JPanel();
            textFieldHolder.setOpaque(false);
            textFieldHolder.setLayout(new BorderLayout());
            textFieldHolder.setBorder(BorderFactory.createTitledBorder(null, "Player Name",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)),
                    Color.WHITE));
            textFieldHolder.add(mNameTextField);

            mNameTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(20)));

            panel.add(textFieldHolder);

            // At least two players must be added
            if (suspects.size() > 4) {
                cancelButton.setVisible(false);
            } else {
                cancelButton.setText("No more players");
            }
        }

        if (suspects != null) {
            mSuspectCardPicker = new CardPickerPanel<>("Suspects", suspects);

            panel.add(mSuspectCardPicker);
        }

        if (weapons != null) {
            mWeaponCardPicker = new CardPickerPanel<>("Weapons", weapons);

            panel.add(mWeaponCardPicker);
        }

        if (rooms != null) {
            mRoomCardPicker = new CardPickerPanel<>("Rooms", rooms);

            panel.add(mRoomCardPicker);
        }

        if (cards != null) {
            mUserCardPicker = new CardPickerPanel<>("Cards", cards);

            panel.add(mUserCardPicker);

            cancelButton.setVisible(false);
        }

        panel.add(buttonPanel);

        updateConfirmButton();

        getContentPane().setLayout(new GridBagLayout());
        add(panel);

        if (board != null) {
            panel.setBackground(new Color(0, 0, 0, 144));
            setBackground(new Color(0, 0, 0, 144));

            setSize(board.getSize());
            setLocationRelativeTo(board);
        } else {
            pack();
            setLocationRelativeTo(frame);
        }
    }

    private void onButtonClicked(ActionEvent actionEvent) {
        mSuccessful = actionEvent.getSource() == mConfirmButton;
        dispose();
    }

    private void updateConfirmButton() {
        mConfirmButton.setEnabled((mSuspectCardPicker == null || mSuspectCardPicker.getResult() != null) &&
                (mWeaponCardPicker == null || mWeaponCardPicker.getResult() != null) &&
                (mRoomCardPicker == null || mRoomCardPicker.getResult() != null) &&
                (mUserCardPicker == null || mUserCardPicker.getResult() != null)
        );

        mConfirmButton.requestFocus();
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

    public static Player showPlayerPickerDialog(JFrame frame, JComponent board, List<Suspect> suspects) {
        CardPickerDialog dialog =  new CardPickerDialog(frame, board, "Add player", true, suspects, null, null, null);

        dialog.setTitle("Add Players");
        dialog.setVisible(true);

        return dialog.getPlayerResult();
    }

    private Suggestion getAccusationResult() {
        if (!mSuccessful) return null;

        Suspect suspect = mSuspectCardPicker.getResult();
        Weapon weapon = mWeaponCardPicker.getResult();
        Room room = mRoomCardPicker.getResult();

        return new Suggestion(suspect, weapon, room);
    }

    public static Suggestion showAccusationPickerDialog(JFrame frame, JComponent board, List<Suspect> suspects, List<Weapon> weapons, List<Room> rooms) {
        CardPickerDialog dialog = new CardPickerDialog(frame, board, "Make final accusation", false, suspects, weapons, rooms, null);

        dialog.setVisible(true);

        return dialog.getAccusationResult();
    }

    private Suggestion getSuggestionResult(Room room) {
        if (!mSuccessful) return null;

        Suspect suspect = mSuspectCardPicker.getResult();
        Weapon weapon = mWeaponCardPicker.getResult();

        return new Suggestion(suspect, weapon, room);
    }

    public static Suggestion showSuggestionPickerDialog(JFrame frame, JComponent board, List<Suspect> suspects, List<Weapon> weapons, Room room) {
        CardPickerDialog dialog = new CardPickerDialog(frame, board, "Make suggestion in " + room.getName(), false, suspects, weapons, null, null);

        dialog.setVisible(true);

        return dialog.getSuggestionResult(room);
    }

    private Card getCardResult() {
        if (!mSuccessful) return null;

        return mUserCardPicker.getResult();
    }

    public static Card showCardPickerDialog(JFrame frame, JComponent board, List<? extends Card> cards) {
        CardPickerDialog dialog = new CardPickerDialog(frame, board, "Choose card to show", false, null, null, null, cards);

        dialog.setVisible(true);

        return dialog.getCardResult();
    }

    public static void showCardResponseDialog(JFrame frame, JComponent board, Player player, Card card) {
        new CardPickerDialog(frame, board, player.getName() + " showed", false, null, null, null, List.of(card)).setVisible(true);
    }

    private class CardPickerPanel<T extends Card> extends JPanel {
        private List<CardPickerCardComponent> mComponents = new ArrayList<>();

        private T mResult;

        private CardPickerPanel(String title, List<T> cards) {
            super();

            setOpaque(false);

            // Setup border
            TitledBorder border = BorderFactory.createTitledBorder(null, title,
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(16)),
                    Color.WHITE);

            setBorder(border);

            // Add components
            for (T c : cards) {
                CardPickerCardComponent component = new CardPickerCardComponent(c, cards.size() > 6 ? 6.0 / cards.size() : 1);
                mComponents.add(component);

                add(component);
            }

            // Pre-select if only one card
            if (cards.size() == 1) {
                mResult = cards.get(0);
                mComponents.get(0).setSelected(true);
            }

            // Minimum width
            if (cards.size() < 3) {
                Insets borderInsets = border.getBorderInsets(this);
                Dimension cardSize = mComponents.get(0).getPreferredSize();
                int width = cardSize.width * 3 + 2 * ((FlowLayout) getLayout()).getHgap() + borderInsets.left + borderInsets.right;
                Dimension preferredSize = getPreferredSize();
                preferredSize.width = width;
                setPreferredSize(preferredSize);
            }

        }

        private void onCardComponentSelected(CardPickerCardComponent cardComponent) {
            mResult = cardComponent.getCard();
            CardPickerDialog.this.updateConfirmButton();

            for (CardPickerCardComponent component : mComponents) {
                if (component == cardComponent) continue;

                component.setSelected(false);
            }
        }

        private T getResult() {
            return mResult;
        }

        private class CardPickerCardComponent extends JComponent {
            private T mCard;
            private Font mFont;

            private BufferedImage mImage;

            private boolean mMouseOver = false;
            private boolean mSelected = false;

            private CardPickerCardComponent(T card, double relativeSize) {
                mCard = card;

                mFont = Config.FONT.deriveFont(Font.PLAIN, Config.screenRelativeSize(16));

                mImage = ImageUtils.getScaledImage(mCard.getCardImage(), (int) Config.screenRelativeSize(150.0 * relativeSize));

                setPreferredSize(new Dimension(mImage.getWidth(), mImage.getHeight() + Config.screenRelativeSize(20)));

                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        setSelected(true);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        setMouseOver(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        setMouseOver(false);
                    }
                });
            }

            public T getCard() {
                return mCard;
            }

            @Override
            public void paintComponent(Graphics gg) {
                Graphics2D g = (Graphics2D) gg;
                Util.setHighQualityRenderingHints(g);

                boolean rotate = mCard instanceof Suspect && (mSelected || mMouseOver);

                // Draw base card
                if (rotate) g.rotate(Math.PI, mImage.getWidth() / 2, mImage.getHeight() / 2);
                g.drawImage(mImage, 0, 0, null);
                if (rotate) g.rotate(Math.PI, mImage.getWidth() / 2, mImage.getHeight() / 2);


                // Draw selected/highlighted overlays
                if (mSelected) {
                    g.drawImage(Card.getCardSelectedOverlayImage(), 0, 0, mImage.getWidth(), mImage.getHeight(), null);
                } else if (mMouseOver) {
                    g.drawImage(Card.getCardHighlightOverlayImage(), 0, 0, mImage.getWidth(), mImage.getHeight(), null);
                }

                // Draw card name
                g.setFont(mFont);
                g.setColor(Color.WHITE);
                Util.drawCenteredString(mCard.getName(), 0, getHeight() - mFont.getSize(), getWidth(), mFont.getSize(), g);
            }

            private void setMouseOver(boolean mouseOver) {
                mMouseOver = mouseOver;
                repaint();
            }

            private void setSelected(boolean selected) {
                if (selected) {
                    CardPickerPanel.this.onCardComponentSelected(this);
                }

                mSelected = selected;
                repaint();
            }
        }
    }
}
