/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.ui.panel;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Knowledge;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.PlayerList;
import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesPanel extends JPanel {
    private PlayerList mPlayers;
    private List<Card> mCards;

    private Knowledge mKnowledge;

    private List<PlayerToken> mPlayerTokens = new ArrayList<>();
    private Map<Card, CardLabel> mCardLabels = new HashMap<>();
    private Map<Card, List<CheckBox>> mCheckBoxes = new HashMap<>();
    
    public NotesPanel(PlayerList players, BoardModel board, List<? extends Card> undistributedCards){
        super();

        mCards = board.getCards();
        mPlayers = players;
        
        setOpaque(false);
        setLayout(new GridBagLayout());

        int gridY = 0;

        // Suspects
        addSection("Suspects", board.getSuspects(), undistributedCards, gridY);
        gridY += board.getSuspects().size() + 1;

        // Weapons
        addSection("Weapons", board.getWeapons(), undistributedCards, gridY);
        gridY += board.getWeapons().size() + 1;

        // Rooms
        addSection("Rooms", board.getRooms(), undistributedCards, gridY);

        // Intercept mouse clicks
        addMouseListener(new MouseAdapter() {});
    }

    public void setCurrentPlayer(Player player) {
        mKnowledge = player.getKnowledge();

        for (int i = 0; i < mPlayerTokens.size(); i++) {
            int playerIndex = i % (mPlayers.size() - 1);
            mPlayerTokens.get(i).setPlayer(mPlayers.getAfter(playerIndex, player));
        }

        for (Card card : mCards) {
            mCardLabels.get(card).updateStatus();

            List<CheckBox> checkBoxes = mCheckBoxes.get(card);
            for (int i = 0; i < mPlayers.size() - 1; i++) {
                checkBoxes.get(i).setPlayer(mPlayers.getAfter(i, player));
            }
        }
    }

    public void updateCards(List<Card> cards) {
        cards.forEach(this::updateCard);
    }

    private void updateCard(Card card) {
        List<CheckBox> checkBoxes = mCheckBoxes.get(card);
        checkBoxes.forEach(CheckBox::updateStatus);
        mCardLabels.get(card).updateStatus();
    }

    private void addSection(String title, List<? extends Card> cards, List<? extends Card> undistributedCards, int gridY){
        // Section title
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridy = gridY;

        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.screenRelativeSize(32)));
        add(sectionLabel, c);

        // Player tokens
        c = new GridBagConstraints();
        c.gridy = gridY++;
        c.insets = new Insets(0, Config.screenRelativeSize(2), 0, Config.screenRelativeSize(2));
        for (int i = 0; i < mPlayers.size() - 1; i++) {
            PlayerToken token = new PlayerToken(Config.screenRelativeSize(26));
            mPlayerTokens.add(token);
            add(token, c);
        }

        // Cards
        for (Card card : cards) {
            // Skip undistributed cards
            if (undistributedCards.contains(card)) {
                continue;
            }

            // Card label
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.LINE_START;
            c.insets = new Insets(0, 0, 0, Config.screenRelativeSize(2));
            c.gridy = gridY;

            CardLabel cardLabel = new CardLabel(card);
            cardLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, Config.screenRelativeSize(18)));
            mCardLabels.put(card, cardLabel);
            add(cardLabel, c);

            // Checkboxes
            List<CheckBox> checkBoxes = new ArrayList<>();
            c = new GridBagConstraints();
            c.gridy = gridY++;
            c.insets = new Insets(0, 0, 1, 0);
            for (int i = 0; i < mPlayers.size() - 1; i++) {
                CheckBox checkBox = new CheckBox(card, Config.screenRelativeSize(24));
                checkBoxes.add(checkBox);
                add(checkBox, c);
            }

            mCheckBoxes.put(card, checkBoxes);
        }
    }

    private static class PlayerToken extends JComponent {
        private Color mColor;

        private PlayerToken(int size) {
            super();

            setPreferredSize(new Dimension(size, size));
        }

        private void setPlayer(Player player) {
            mColor = player.getCharacter().getColor();
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(mColor);
            g.fillOval(0, 0, getWidth(), getHeight());
        }
    }

    private static final Map<Knowledge.Value, String> VALUE_ICONS = new HashMap<>() {{
        put(Knowledge.Value.Holding, "holding");
        put(Knowledge.Value.NotHolding, "not-holding");
        put(Knowledge.Value.SuspectedHolding, "suspected-holding");
        put(Knowledge.Value.SuspectedNotHolding, "suspected-not-holding");
        put(Knowledge.Value.Undistributed, "undistributed");
        put(Knowledge.Value.Self, "self");
    }};

    private static final Map<Knowledge.Value, String> VALUE_NAMES = new HashMap<>() {{
        put(null, "Unknown");
        put(Knowledge.Value.Holding, "Holding Card");
        put(Knowledge.Value.NotHolding, "Not Holding Card");
        put(Knowledge.Value.SuspectedHolding, "Suspected Holding");
        put(Knowledge.Value.SuspectedNotHolding, "Suspected Not Holding");
    }};

    private static BufferedImage getValueIcon(Knowledge.Value value) {
        return ImageUtils.loadImage("icons/notes/" + VALUE_ICONS.get(value) + ".png");
    }

    private static BufferedImage getHintIcon(int hint) {
        return ImageUtils.loadImage("icons/notes/hint-" + (hint + 1) + ".png");
    }

    private static final Color CARD_LABEL_COLOR_CORRECT = Color.decode("#006600");

    private class CardLabel extends JComponent {
        private Card mCard;
        private boolean mStrike;
        private boolean mCorrect;

        private CardLabel(Card card) {
            super();

            mCard = card;
        }

        @Override
        public void setFont(Font font) {
            super.setFont(font);

            FontMetrics metrics = getFontMetrics(font);
            int width = metrics.stringWidth(mCard.getName());
            int height = metrics.getHeight();

            setPreferredSize(new Dimension(width, height));
        }

        private void updateStatus() {
            mStrike = mKnowledge.get(mCard).values().stream()
                    .map(Knowledge.Status::getValue)
                    .anyMatch(v -> v == Knowledge.Value.Holding || v == Knowledge.Value.Undistributed || v == Knowledge.Value.Self);

            mCorrect = mKnowledge.get(mCard).values().stream()
                    .map(Knowledge.Status::getValue)
                    .allMatch(Knowledge.Value.NotHolding::equals);

            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(mCorrect ? CARD_LABEL_COLOR_CORRECT : Color.BLACK);
            Util.drawCenteredString(mCard.getName(), 0, 0, -1, getHeight(), g);

            if (mStrike) {
                g.setColor(Color.BLACK);
                g.fillRect(0, getHeight() / 2, g.getFontMetrics().stringWidth(mCard.getName()), Config.screenRelativeSize(2));
            }
        }
    }

    private static final Border CHECKBOX_DEFAULT_BORDER = BorderFactory.createLineBorder(Color.BLACK, Config.screenRelativeSize(2));
    private static final Border CHECKBOX_FIXED_BORDER = BorderFactory.createLineBorder(Color.GRAY, Config.screenRelativeSize(2));
    private static final Border CHECKBOX_ACTIVE_BORDER = BorderFactory.createLineBorder(Color.ORANGE, Config.screenRelativeSize(3));

    private class CheckBox extends JComponent {
        private static final double MARGIN = 0.1;

        private Knowledge.Status mStatus;

        private Card mCard;
        private Player mPlayer;
        private BufferedImage mImage;

        private CheckBox(Card card, int size) {
            super();

            mCard = card;

            setBorder(CHECKBOX_DEFAULT_BORDER);
            setPreferredSize(new Dimension(size, size));
            setSize(getPreferredSize());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showPopup();
                }
            });
        }

        private void setPlayer(Player player) {
            mPlayer = player;
            updateStatus();
        }

        private void updateStatus() {
            mStatus = mKnowledge.get(mCard).get(mPlayer);

            if (mStatus.getValue() != null) {
                mImage = ImageUtils.getScaledImage(getValueIcon(mStatus.getValue()), (int) (getWidth() * (1.0 - 2 * MARGIN)));
            } else {
                mImage = null;
            }

            boolean popupEnabled = mStatus.getValue() == Knowledge.Value.Undistributed || mStatus.getValue() == Knowledge.Value.Self;
            setCursor(Cursor.getPredefinedCursor(popupEnabled ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
            setBorder(mStatus.isFixed() ? CHECKBOX_FIXED_BORDER : CHECKBOX_DEFAULT_BORDER);

            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            for (int hint = 0; hint < 4; hint++) {
                if (mStatus.getHints()[hint]) {
                    BufferedImage hintImage = ImageUtils.getScaledImage(getHintIcon(hint), getWidth());
                    g.drawImage(hintImage, 0, 0, null);
                }
            }

            if (mImage != null) {
                g.drawImage(mImage, (int) (getWidth() * MARGIN), (int) (getHeight() * MARGIN), null);
            }
        }

        private void showPopup() {
            // Don't show popup on undistributed or
            if (mStatus.getValue() == Knowledge.Value.Undistributed || mStatus.getValue() == Knowledge.Value.Self) {
                return;
            }

            // Don't show popup if status is fixed
            JPopupMenu menu = new JPopupMenu();
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(20));

            // Include suspect name if player name different
            String playerName = mPlayer.getName();
            if (!mPlayer.getName().equals(mPlayer.getCharacter().getName())) {
                playerName += " [" + mPlayer.getCharacter().getName() + "]";
            }

            // Add disabled player item
            JMenuItem playerItem = new JMenuItem(playerName);
            playerItem.setFont(font);
            playerItem.setEnabled(false);
            menu.add(playerItem);

            // Add values
            if (!mStatus.isFixed()) {
                for (Knowledge.Value value : VALUE_NAMES.keySet()) {
                    JMenuItem item;
                    if (value != null) {
                        BufferedImage icon = ImageUtils.getScaledImage(getValueIcon(value), Config.screenRelativeSize(20));
                        item = new JMenuItem(VALUE_NAMES.get(value), new ImageIcon(icon));
                    } else {
                        item = new JMenuItem(VALUE_NAMES.get(null));
                    }
                    item.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(20)));
                    item.addActionListener(e -> {
                        mKnowledge.setValue(mCard, mPlayer, value, false);

                        // Other player values may have changed, so update entire row
                        updateCard(mCard);
                    });
                    menu.add(item);
                }
            }

            menu.addSeparator();

            // Add hints
            for (int i = 0; i < 4; i++) {
                int hint = i;
                BufferedImage icon = ImageUtils.getScaledImage(getHintIcon(hint), Config.screenRelativeSize(20));
                JCheckBoxMenuItem item = new JCheckBoxMenuItem("Hint " + (hint + 1), new ImageIcon(icon));
                item.setState(mStatus.getHints()[hint]);
                item.setFont(font);
                item.addItemListener(e -> {
                    mKnowledge.toggleHint(mCard, mPlayer, hint);

                    // Redraw hints
                    updateStatus();
                });
                menu.add(item);
            }

            menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    setBorder(CHECKBOX_ACTIVE_BORDER);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    setBorder(mStatus.isFixed() ? CHECKBOX_FIXED_BORDER : CHECKBOX_DEFAULT_BORDER);
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });

            menu.show(this, getWidth(), getHeight());
        }
    }
}
