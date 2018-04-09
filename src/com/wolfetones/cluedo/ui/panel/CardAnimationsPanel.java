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

import com.wolfetones.cluedo.util.ImageUtils;
import com.wolfetones.cluedo.util.Util;
import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Weapon;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Suggestion;
import com.wolfetones.cluedo.ui.Animator;
import com.wolfetones.cluedo.ui.component.ImageComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CardAnimationsPanel extends JPanel {
    private static final double CARD_MARGIN = 0.9;

    private static Random sRandom = new Random();

    public CardAnimationsPanel() {
        super();

        setLayout(null);
        setOpaque(false);
    }

    public void dealCards(int playerCount, int playerPanelItemHeight, List<? extends Card> remainingCards, BoardModel board) {
        setVisible(true);

        List<Suspect> suspects = board.getSuspects();
        List<Weapon> weapons = board.getWeapons();
        List<Room> rooms = board.getRooms();

        int cardWidth = (int) (getWidth() / suspects.size() * CARD_MARGIN);

        // Create cards
        List<AnimatableCard> suspectCards = suspects.stream()
                .map((c) -> new AnimatableCard(c, cardWidth))
                .collect(Collectors.toList());

        List<AnimatableCard> weaponCards = weapons.stream()
                .map((c) -> new AnimatableCard(c, cardWidth))
                .collect(Collectors.toList());

        List<AnimatableCard> roomCards = rooms.stream()
                .map((c) -> new AnimatableCard(c, cardWidth))
                .collect(Collectors.toList());

        List<List<AnimatableCard>> allCardLists = List.of(suspectCards, weaponCards, roomCards);
        AnimatableCard lastCard = roomCards.get(roomCards.size() - 1);

        // Add components
        suspectCards.forEach(this::add);
        weaponCards.forEach(this::add);
        roomCards.forEach(this::add);

        for (List<AnimatableCard> list : allCardLists) {
            for (int i = 0; i < list.size(); i++) {
                AnimatableCard card = list.get(i);

                // Reverse z-order
                setComponentZOrder(card, getComponentCount() - getComponentZOrder(card) - 1);

                // Update scales relative to suspects
                card.setScale((double) suspectCards.size() / list.size());

                // Initial state
                card.setScaleX(0);
                card.setScaleY(0);
            }
        }

        // Move from center
        int delay = 0;
        for (int l = 0; l < allCardLists.size(); l++) {
            List<AnimatableCard> list = allCardLists.get(l);
            for (int i = 0; i < list.size(); i++) {
                AnimatableCard card = list.get(i);

                int cardWidthSection = getWidth() / list.size();
                int cardHeightSection = getHeight() / 3;

                double targetX = cardWidthSection * (i + 0.5);
                double targetY = cardHeightSection * (l + 0.5);

                Animator.getInstance().animate(card)
                        .animate(getWidth() / 2, targetX, card::setCenterX)
                        .animate(getHeight() / 2, targetY, card::setCenterY)
                        .scale(1, 1)
                        .fadeIn()
                        .setDuration(1000)
                        .setDelay(delay++ * 100)
                        .skipIf(!isVisible())
                        .awaitIf(card == lastCard);
            }
        }

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(2000)
                .skipIf(!isVisible())
                .await();

        // Flip all cards
        delay = 0;
        for (List<AnimatableCard> list : allCardLists) {
            for (AnimatableCard card : list) {
                Animator.getInstance().animate(card)
                        .scale(-1, 1)
                        .setDuration(500)
                        .setDelay(delay++ * 50)
                        .skipIf(!isVisible())
                        .awaitIf(card == lastCard);
            }
        }

        // Move all cards to piles side
        for (List<AnimatableCard> list : allCardLists) {
            for (int i = 0; i < list.size(); i++) {
                AnimatableCard card = list.get(i);

                int offset = (i - list.size() / 2) * Config.screenRelativeSize(10);
                int targetX = getWidth() * 3 / 4 + offset;
                int targetY = (int) (card.getCenterY() + offset);

                Animator.getInstance().animate(card)
                        .animate(card::getCenterX, targetX, card::setCenterX)
                        .animate(card::getCenterY, targetY, card::setCenterY)
                        .setDuration(2000)
                        .skipIf(!isVisible())
                        .awaitIf(card == lastCard);
            }
        }

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(500)
                .await();

        // Select random suspect, weapon and room
        AnimatableCard chosenSuspect = suspectCards.remove(sRandom.nextInt(suspectCards.size()));
        AnimatableCard chosenWeapon = weaponCards.remove(sRandom.nextInt(weaponCards.size()));
        AnimatableCard chosenRoom = roomCards.remove(sRandom.nextInt(roomCards.size()));
        List<AnimatableCard> chosenCards = List.of(chosenSuspect, chosenWeapon, chosenRoom);

        // Add secret folder component
        ImageComponent secretFolder = new ImageComponent(ImageUtils.loadImage("envelope.png"), getWidth() / 4);
        add(secretFolder);
        setComponentZOrder(secretFolder, 0);
        secretFolder.setLocation(getWidth() / 3 - secretFolder.getWidth() / 2, getHeight());

        // Move chosen cards to center
        for (int i = 0; i < chosenCards.size(); i++) {
            AnimatableCard card = chosenCards.get(i);

            double targetX = getWidth() / 3 + cardWidth / CARD_MARGIN * (i - 1);

            Animator.getInstance().animate(card)
                    .animate(card::getCenterX, targetX, card::setCenterX)
                    .animate(card::getCenterY, getHeight() / 2, card::setCenterY)
                    .scale(1)
                    .setDuration(2000)
                    .skipIf(!isVisible())
                    .awaitIf(card == chosenRoom);
        }

        // Move secret folder into view
        Animator.getInstance().animate(secretFolder)
                .translate(secretFolder.getX(), getHeight() - secretFolder.getHeight())
                .setDuration(1000)
                .skipIf(!isVisible())
                .await();

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(500)
                .skipIf(!isVisible())
                .await();

        // Move chosen cards to secret folder
        for (int i = 0; i < chosenCards.size(); i++) {
            AnimatableCard card = chosenCards.get(i);

            int targetX = getWidth() / 3 + Config.screenRelativeSize(10) * (i - 1);
            int targetY = getHeight() * 4 / 5;

            Animator.getInstance().animate(card)
                    .animate(card::getCenterX, targetX, card::setCenterX)
                    .setDuration(500)
                    .chain()
                    .animate(card::getCenterY, targetY, card::setCenterY)
                    .setDelay(100)
                    .setDuration(500)
                    .after(() -> card.setVisible(false))
                    .skipIf(!isVisible())
                    .awaitIf(card == chosenRoom);
        }

        // Hide secret folder
        Animator.getInstance().animate(secretFolder)
                .translate(secretFolder.getX(), getHeight())
                .setDelay(1000)
                .setDuration(500)
                .skipIf(!isVisible())
                .await();

        // Randomly distribute cards from stacks to all cards stack, maintaining order within stack
        List<AnimatableCard> allCards = new ArrayList<>();
        while (!suspectCards.isEmpty() || !weaponCards.isEmpty() || !roomCards.isEmpty()) {
            List<AnimatableCard> list = allCardLists.get(sRandom.nextInt(3));
            if (list.isEmpty()) {
                continue;
            }

            AnimatableCard card = list.remove(0);

            allCards.add(card);
            setComponentZOrder(card, allCards.size());
        }

        // Update new last card
        lastCard = allCards.get(allCards.size() - 1);

        // Move unselected cards to single pile
        for (int i = 0; i < allCards.size(); i++) {
            AnimatableCard card = allCards.get(i);

            int offset = (i - allCards.size() / 2) * Config.screenRelativeSize(10);
            int targetX = getWidth() / 2 + offset;
            int targetY = getHeight() / 2 + offset;

            Animator.getInstance().animate(card)
                    .animate(card::getCenterX, targetX, card::setCenterX)
                    .animate(card::getCenterY, targetY, card::setCenterY)
                    .scale(1.0)
                    .setDuration(2000)
                    .skipIf(!isVisible())
                    .awaitIf(card == lastCard);
        }

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(500)
                .skipIf(!isVisible())
                .await();

        // Distribute cards to players
        delay = 0;
        for (int i = 0; i < allCards.size() - remainingCards.size(); i++) {
            AnimatableCard card = allCards.get(i);

            int round = i / playerCount;
            int player = i % playerCount;

            double targetX = card.getWidth() * 0.15 + round * Config.screenRelativeSize(10);
            double targetY = ((double) player + 0.5) * playerPanelItemHeight;

            Animator.getInstance().animate(card)
                    .animate(card::getCenterX, targetX, card::setCenterX)
                    .animate(card::getCenterY, targetY, card::setCenterY)
                    .scale(0.3)
                    .setDuration(500)
                    .setDelay(delay++ * 100)
                    .skipIf(!isVisible())
                    .awaitIf(i == allCards.size() - remainingCards.size() - 1);
        }

        // Show the remaining cards
        if (!remainingCards.isEmpty()) {
            delay = 0;
            for (int i = 0; i < remainingCards.size(); i++) {
                AnimatableCard card = allCards.get(i + allCards.size() - remainingCards.size());

                card.setImage(remainingCards.get(i).getCardImage());

                double targetX = getWidth() / 2 + (((double) remainingCards.size() - 1) / 2.0 - i) * card.getWidth();
                double targetY = getHeight() / 2;

                Animator.getInstance().animate(card)
                        .animate(card::getCenterX, targetX, card::setCenterX)
                        .animate(card::getCenterY, targetY, card::setCenterY)
                        .scale(1.0)
                        .scale(1.0, 1.0)
                        .setDuration(1000)
                        .setDelay(delay++ * 200)
                        .skipIf(!isVisible())
                        .awaitIf(card == lastCard);
            }
        }

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(3000)
                .skipIf(!isVisible())
                .await();

        // Fade out all cards
        for (AnimatableCard card : allCards) {
            Animator.getInstance().animate(card)
                    .fadeOut()
                    .setInterpolator(Animator::easeInCubic)
                    .setDuration(250)
                    .skipIf(!isVisible())
                    .awaitIf(card == lastCard);
        }

        // Remove all added components
        removeAll();

        setVisible(false);
    }

    public void finalAccusation(Suggestion accusation, Suggestion solution) {
        setVisible(true);

        boolean correct = accusation.equals(solution);

        int cardWidth = (int) (getWidth() / 5 * CARD_MARGIN);

        List<Card> accusationList = accusation.asList();
        List<Card> solutionList = solution.asList();

        List<AnimatableCard> accusationCards = accusationList.stream()
                .map((c) -> new AnimatableCard(c, cardWidth))
                .collect(Collectors.toList());

        List<AnimatableCard> solutionCards = solutionList.stream()
                .map((c) -> new AnimatableCard(c, cardWidth))
                .collect(Collectors.toList());

        accusationCards.forEach(this::add);
        solutionCards.forEach(this::add);

        // Move accusation cards to middle
        for (int i = 0; i < accusationCards.size(); i++) {
            AnimatableCard card = accusationCards.get(i);

            card.setCenterX((getWidth() / 4) * (i + 1));
            card.setCenterY(-card.getHeight() / 2);

            int targetY = getHeight() / 5;

            Animator.getInstance().animate(card)
                    .animate(card::getCenterY, targetY, card::setCenterY)
                    .fadeIn()
                    .setInterpolator(Animator::easeOutQuint)
                    .setDuration(2000)
                    .setDelay(i * 1000)
                    .skipIf(!isVisible())
                    .awaitIf(i == accusationCards.size() - 1);
        }

        // Add secret folder component
        ImageComponent secretFolder = new ImageComponent(ImageUtils.loadImage("envelope.png"), getWidth() / 4);
        add(secretFolder);
        setComponentZOrder(secretFolder, 0);
        secretFolder.setLocation(getWidth() / 2 - secretFolder.getWidth() / 2, getHeight());

        // Move secret folder in to view
        Animator.getInstance().animate(secretFolder)
                .translate(secretFolder.getX(), getHeight() - secretFolder.getHeight())
                .setDuration(1000)
                .skipIf(!isVisible())
                .await();

        // Move solution cards out of secret folder
        for (int i = 0; i < solutionCards.size(); i++) {
            AnimatableCard card = solutionCards.get(i);

            card.setAlpha(1.0);
            card.setScaleX(-1);
            card.setCenterX(secretFolder.getX() + secretFolder.getWidth() / 2);
            card.setCenterY(secretFolder.getY() + secretFolder.getHeight() / 2);

            int targetX = (getWidth() / 4) * (i + 1);
            int targetY = getHeight() / 2;

            int cardIndex = i;
            boolean cardCorrect = accusationList.get(cardIndex) == solutionList.get(cardIndex);
            BufferedImage overlayImage = cardCorrect ? Card.getCardCorrectOverlayImage() : Card.getCardIncorrectOverlayImage();
            Animator.getInstance().animate(card)
                    .animate(card::getCenterX, targetX, card::setCenterX)
                    .animate(card::getCenterY, targetY, card::setCenterY)
                    .setInterpolator(Animator::easeOutQuint)
                    .setDuration(2000)
                    .chain()
                    .scale(1, 1)
                    .after(() -> {
                        solutionCards.get(cardIndex).setOverlayImage(overlayImage);
                        accusationCards.get(cardIndex).setOverlayImage(overlayImage);
                    })
                    .setDuration(800)
                    .setDelay(1000 + i * 2000)
                    .skipIf(!isVisible())
                    .awaitIf(i == solutionCards.size() - 1);
        }

        // Wait
        Animator.getInstance().animate(this)
                .setDelay(3000)
                .skipIf(!isVisible())
                .await();

        if (!correct) {
            // Move solution cards back into secret folder
            for (int i = 0; i < solutionCards.size(); i++) {
                AnimatableCard card = solutionCards.get(i);

                int targetX = secretFolder.getX() + secretFolder.getWidth() / 2;
                int targetY = secretFolder.getY() + secretFolder.getHeight() / 2;

                Animator.getInstance().animate(card)
                        .scale(-1, 1)
                        .setDuration(800)
                        .chain()
                        .animate(card::getCenterX, targetX, card::setCenterX)
                        .animate(card::getCenterY, targetY, card::setCenterY)
                        .setDuration(2000)
                        .setDelay(100)
                        .after(() -> card.setVisible(false))
                        .awaitIf(i == solutionCards.size() - 1);
            }

            // Move secret folder out of view
            Animator.getInstance().animate(secretFolder)
                    .translate(secretFolder.getX(), getHeight())
                    .setDuration(1000)
                    .setDelay(100)
                    .start();

            // Move accusation cards out of view
            for (int i = 0; i < accusationCards.size(); i++) {
                AnimatableCard card = accusationCards.get(i);

                Animator.getInstance().animate(card)
                        .animate(card::getCenterY, -card.getHeight() / 2, card::setCenterY)
                        .setDuration(1000)
                        .setDelay(100)
                        .awaitIf(i == accusationCards.size() - 1);
            }

            AnimatableLabel youLoseLabel = new AnimatableLabel("Incorrect, you lose!", Config.screenRelativeSize(75));
            add(youLoseLabel);
            youLoseLabel.setCenterX(getWidth() / 2);
            youLoseLabel.setCenterY(getHeight() / 2);
            youLoseLabel.setScale(1);
            youLoseLabel.setAlpha(0);

            Animator.getInstance().animate(youLoseLabel)
                    .fadeIn()
                    .setDuration(1500)
                    .setInterpolator(Animator::easeInCubic)
                    .chain()
                    .fadeOut()
                    .setDuration(1500)
                    .setDelay(1500)
                    .setInterpolator(Animator::easeOutCubic)
                    .await();

            removeAll();

            setVisible(false);
        } else {
            // Move secret folder out of view
            Animator.getInstance().animate(secretFolder)
                    .translate(secretFolder.getX(), getHeight())
                    .setDuration(1000)
                    .setDelay(100)
                    .start();

            // Move solution cards to bottom of screen
            for (int i = 0; i < solutionCards.size(); i++) {
                AnimatableCard card = solutionCards.get(i);

                int targetY = getHeight() * 4 / 5;

                Animator.getInstance().animate(card)
                        .animate(card::getCenterY, targetY, card::setCenterY)
                        .setDuration(1000)
                        .setDelay(500)
                        .awaitIf(i == solutionCards.size() - 1);
            }

            AnimatableLabel youWinLabel = new AnimatableLabel("Congratulations, you win!", Config.screenRelativeSize(60));
            add(youWinLabel);
            youWinLabel.setCenterX(getWidth() / 2);
            youWinLabel.setCenterY(getHeight() / 2);
            youWinLabel.setScale(1);
            youWinLabel.setAlpha(0);

            Animator.getInstance().animate(youWinLabel)
                    .fadeIn()
                    .setDuration(1500)
                    .setInterpolator(Animator::easeInCubic)
                    .start();
        }
    }

    public void forceFinish() {
        setVisible(false);

        Animator.getInstance().interruptAllAnimations(this);
        for (Component component : getComponents()) {
            Animator.getInstance().interruptAllAnimations(component);
        }
    }

    private class AnimatableLabel extends AnimatableComponent {
        private String mText;

        public AnimatableLabel(String text, int fontSize) {
            super();

            mText = text;

            setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));

            FontMetrics metrics = getFontMetrics(getFont());
            int width = metrics.stringWidth(mText);
            int height = metrics.getHeight();

            setPreferredSize(new Dimension(width, height));
            setSize(getPreferredSize());
        }

        @Override
        public void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;
            Util.setHighQualityRenderingHints(g);

            if (!applyTransform(g)) {
                return;
            }

            Util.drawCenteredString(mText, 0, 0, getWidth(), getHeight(), g);
        }
    }

    private class AnimatableCard extends AnimatableComponent {
        private BufferedImage mImage;
        private BufferedImage mOverlayImage;
        private BufferedImage mBackImage;

        private String mName;

        private int mOffsetY;

        public AnimatableCard(Card card, int imageWidth) {
            super();

            mName = card.getName();

            mImage = ImageUtils.getScaledImage(card.getCardImage(), imageWidth);
            setFont(Config.FONT.deriveFont(Font.PLAIN, Config.screenRelativeSize(20)));

            mBackImage = ImageUtils.getScaledImage(Card.getCardBackImage(), imageWidth);

            int imageHeight = mImage.getHeight();
            int defaultHeight = imageHeight + 2 * (Config.screenRelativeSize(5) + getFont().getSize());

            mOffsetY = (defaultHeight - imageHeight) / 2;

            setPreferredSize(new Dimension(imageWidth, defaultHeight));
            setSize(getPreferredSize());
        }

        public void setImage(BufferedImage image) {
            mImage = ImageUtils.getScaledImage(image, mImage.getWidth(), mImage.getHeight());
            repaint();
        }

        public void setOverlayImage(BufferedImage image) {
            mOverlayImage = ImageUtils.getScaledImage(image, mImage.getWidth(), mImage.getHeight());
            repaint();
        }

        @Override
        public void paintComponent(Graphics gg) {
            Graphics2D g = (Graphics2D) gg;
            Util.setHighQualityRenderingHints(g);

            if (!applyTransform(g)) {
                return;
            }

            if (mScaleX > 0) {
                g.drawImage(mImage, 0, mOffsetY, null);

                if (mOverlayImage != null) {
                    g.drawImage(mOverlayImage, 0, mOffsetY, null);
                }

                g.setColor(Color.WHITE);
                Util.drawCenteredString(mName, 0, mOffsetY + mImage.getHeight() + Config.screenRelativeSize(5), mImage.getWidth(), getFont().getSize(), g);
            } else {
                g.drawImage(mBackImage, 0, mOffsetY, null);
            }
        }
    }

    private class AnimatableComponent extends JComponent implements Animator.Scalable, Animator.ScalableXY, Animator.Fadable {
        protected double mScale = 1;
        protected double mScaleX = 1;
        protected double mScaleY = 1;
        protected double mAlpha = 0;

        public AnimatableComponent() {
            super();
        }

        public double getCenterX() {
            return getX() + getWidth() / 2;
        }

        public double getCenterY() {
            return getY() + getHeight() / 2;
        }

        public void setCenterX(double x) {
            setLocation((int) (x - getWidth() / 2), getY());
        }

        public void setCenterY(double y) {
            setLocation(getX(), (int) (y - getHeight() / 2));
        }

        protected boolean applyTransform(Graphics2D g) {
            if (mAlpha == 0) {
                return false;
            } else if (mAlpha < 1) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) mAlpha));
            }

            double scaleX = Math.abs(mScaleX) * mScale;
            double scaleY = mScaleY * mScale;
            if (scaleX == 0 || scaleY == 0) {
                return false;
            } else if (scaleX != 1 || scaleY != 1) {
                Dimension preferredSize = getPreferredSize();
                g.translate(getWidth() / 2, getHeight() / 2);
                g.scale(scaleX, scaleY);
                g.translate(-preferredSize.width / 2, -preferredSize.height / 2);
            }

            return true;
        }

        @Override
        public double getScale() {
            return mScale;
        }

        @Override
        public void setScale(double scale) {
            Dimension preferredSize = getPreferredSize();

            int scaledWidth = (int) (preferredSize.width * scale);
            int scaledHeight = (int) (preferredSize.height * scale);

            int centerX = (int) getCenterX();
            int centerY = (int) getCenterY();

            setBounds(centerX - scaledWidth / 2, centerY - scaledHeight / 2,
                    scaledWidth, scaledHeight);

            mScale = scale;
            repaint();
        }

        @Override
        public double getScaleX() {
            return mScaleX;
        }

        @Override
        public double getScaleY() {
            return mScaleY;
        }

        @Override
        public void setScaleX(double scale) {
            mScaleX = scale;
            repaint();
        }

        @Override
        public void setScaleY(double scale) {
            mScaleY = scale;
            repaint();
        }

        @Override
        public double getAlpha() {
            return mAlpha;
        }

        @Override
        public void setAlpha(double alpha) {
            mAlpha = alpha;
            repaint();
        }
    }
}
