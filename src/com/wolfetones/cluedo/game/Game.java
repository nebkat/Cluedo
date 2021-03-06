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

package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.Location;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    public static final int NUM_DICE = Boolean.parseBoolean(System.getProperty("debug")) ? 4 : 2;

    private static Random sRandom = new Random();

    // States
    private boolean mStarted = false;
    private boolean mFinished = false;

    /** Cards that have not been distributed to any player (visible to all) */
    private List<Card> mUndistributedCards = new ArrayList<>(0);

    /** Game solution */
    private Suggestion mSolution;

    /**
     * Players
     */
    private PlayerList mPlayers = new PlayerList();
    /** Players not eliminated */
    private List<Player> mActivePlayers;
    private ListIterator<Player> mActivePlayerIterator;

    private Player mCurrentPlayer;
    private Location mCurrentPlayerLocation;

    /*
     * Current abilities in turn, depending on state and location
     */
    private Room mTurnInitialPlayerRoom;
    private boolean mTurnCanRollDice;
    private boolean mTurnCanUsePassage;
    private boolean mTurnCanPoseQuestion;
    private boolean mTurnCanMakeFinalAccusation;
    private boolean mTurnFinished;
    private int mTurnRemainingMoves;
    private boolean mTurnHasMoved;
    private boolean mTurnMovementComplete;
    private Suggestion mTurnQuestionSuggestion;
    private Player mTurnQuestionCardHolder;

    /**
     * Board
     */
    private BoardModel mBoard = new BoardModel();

    /**
     * Log
     */
    private List<LogEntry> mLog = new ArrayList<>();

    public static class LogEntry {
        public enum Type {
            Question, FinalAccusation
        }

        private Type type;

        private Player player;
        private Suggestion suggestion;

        private Player responder;
        private Card response;

        private boolean correct;

        private static LogEntry newQuestionEntry(Player player, Suggestion suggestion, Player responder, Card response) {
            LogEntry entry = new LogEntry();
            entry.type = Type.Question;
            entry.player = player;
            entry.suggestion = suggestion;
            entry.responder = responder;
            entry.response = response;

            return entry;
        }

        private static LogEntry newFinalAccusationEntry(Player player, Suggestion suggestion, boolean correct) {
            LogEntry entry = new LogEntry();
            entry.type = Type.FinalAccusation;
            entry.player = player;
            entry.suggestion = suggestion;
            entry.correct = correct;

            return entry;
        }

        public Type getType() {
            return type;
        }

        public Player getPlayer() {
            return player;
        }

        public Suggestion getSuggestion() {
            return suggestion;
        }

        public Player getResponder() {
            return responder;
        }

        public Card getResponse() {
            return response;
        }

        public boolean isCorrect() {
            return correct;
        }
    }

    /**
     * Add a player to the game.
     *
     * Must be performed before game has started.
     *
     * @param player Player to add to the game.
     * @throws IllegalStateException If the game has already started.
     */
    public void addPlayer(Player player) {
        if (mStarted) {
            throw new IllegalStateException("Players cannot be added once game has started");
        }

        mPlayers.add(player);
    }

    /**
     * Start the game.
     *
     * At least 2 players must be added to start the game.
     *
     * @throws IllegalStateException If the game has already started, or there are not enough players to start the game.
     */
    public void start() {
        if (mStarted) {
            throw new IllegalStateException("Game already started");
        }

        if (mPlayers.size() < 2) {
            throw new IllegalStateException("Not enough players to start game");
        }

        mStarted = true;

        mActivePlayers = new ArrayList<>(mPlayers);
        mActivePlayerIterator = mActivePlayers.listIterator();

        setupCards();
    }

    /**
     * Gets the game board associated with this game.
     *
     * @return the game board associated with this game.
     */
    public BoardModel getBoard() {
        return mBoard;
    }

    /**
     * Gets the {@code Solution} to the game.
     *
     * Used in cheat command, and for graphical display
     *
     * @return the {@code Solution} to the game.
     */
    public Suggestion getSolution() {
        return mSolution;
    }

    /**
     * Gets the list of undistributed cards, which are visible to all players.
     *
     * Suggestions can't be made using any of these cards.
     *
     * @return a list of cards that were not distributed to any player.
     */
    public List<? extends Card> getUndistributedCards() {
        return mUndistributedCards;
    }

    /**
     * Gets the question log.
     *
     * @return the game question log.
     */
    public List<LogEntry> getLog() {
        return mLog;
    }

    /**
     * Moves to the next player and updates states.
     *
     * @return Player who's turn it is.
     */
    public Player nextTurn() {
        // Loop the current player index
        if (!mActivePlayerIterator.hasNext()) {
            mActivePlayerIterator = mActivePlayers.listIterator();
        }

        mCurrentPlayer = mActivePlayerIterator.next();
        mCurrentPlayerLocation = mCurrentPlayer.getCharacter().getLocation();

        // Store to ensure player does not return to the room they started in
        if (mCurrentPlayerLocation.isRoom()) {
            mTurnInitialPlayerRoom = mCurrentPlayerLocation.asRoom();
        } else {
            mTurnInitialPlayerRoom = null;
        }

        // Check if the player is capable of performing a move
        if (mCurrentPlayerLocation.isRoom()) {
            mTurnCanUsePassage = mCurrentPlayerLocation.asRoom().hasPassage();
            mTurnCanRollDice = mCurrentPlayerLocation.asRoom()
                    .getEntranceCorridors()
                    .stream()
                    .map(RoomTile::getDoorTile)
                    .anyMatch(TokenOccupiableTile::isFree);
        } else {
            mTurnCanUsePassage = false;
            mTurnCanRollDice = mCurrentPlayerLocation.asTile().canMove();
        }
        mTurnMovementComplete = !mTurnCanRollDice && !mTurnCanUsePassage;

        // Check if player can guess immediately
        mTurnCanPoseQuestion = mCurrentPlayer.getCharacter().getMovedSinceLastTurn();

        // Should never be able to make accusation as this point
        mTurnCanMakeFinalAccusation = false;

        // Check if player can perform anything (rare case that player is stuck in corridor or room)
        mTurnFinished = mTurnMovementComplete && !mTurnCanPoseQuestion;

        // Reset movement variables
        mTurnHasMoved = false;
        mTurnRemainingMoves = 0;

        // Reset question variables
        mTurnQuestionSuggestion = null;
        mTurnQuestionCardHolder = null;

        return mCurrentPlayer;
    }

    /**
     * Rolls the dice randomly and returns the number of allowed moves.
     *
     * @param dice Array with {@code NUM_DICE} entries to store individual dice values.
     * @return The total number of allowed moves.
     * @throws IllegalStateException If the player has already moved or has nowhere to move to.
     */
    public int rollDice(int[] dice) {
        // Can't roll dice if player has already moved this turn
        if (mTurnMovementComplete) {
            throw new IllegalStateException("Player has already moved");
        }

        // Can't roll dice if nowhere to move to
        if (!mTurnCanRollDice) {
            throw new IllegalStateException("Player has nowhere to move to");
        }

        // Dice array must be of size 2
        if (dice.length != NUM_DICE) {
            throw new IllegalArgumentException("int[] dice must be of size " + NUM_DICE);
        }

        // Random dice roll
        for (int i = 0; i < NUM_DICE; i++) {
            if (dice[i] == 0) {
                dice[i] = sRandom.nextInt(6) + 1;
            }
            mTurnRemainingMoves += dice[i];
        }

        // Can no longer move
        mTurnMovementComplete = true;

        // Can no longer guess (until movement is complete)
        mTurnCanPoseQuestion = false;

        return mTurnRemainingMoves;
    }

    /**
     * Uses a passage in the player's current room.
     *
     * @throws IllegalStateException If player has already moved or player's current room does not have a passage.
     */
    public void usePassage() {
        // Can't use passage if player has already moved this turn
        if (mTurnMovementComplete) {
            throw new IllegalStateException("Player has already moved");
        }

        // Can't use passage if not currently in a room
        if (!mTurnCanUsePassage) {
            throw new IllegalStateException("Player cannot use passage, either not in a room or in a room that does not contain a secret passage");
        }

        Room passageRoom = mCurrentPlayerLocation.asRoom().getPassageRoom();

        PassageTile currentRoomPassageTile = mCurrentPlayerLocation.asRoom().getPassageTile();
        PassageTile passageRoomPassageTile = passageRoom.getPassageTile();

        // Move to new room
        completeMove(mCurrentPlayerLocation.asRoom().getPassageRoom(), List.of(currentRoomPassageTile, passageRoomPassageTile));

        // Can no longer move
        mTurnMovementComplete = true;
    }

    /**
     * Moves the player's {@code Token} to a {@link Location} and subtracts the number of moves required.
     *
     * @param location the player's new location
     * @return The number of moves remaining.
     * @throws IllegalStateException If there are not enough moves available or there is no path to the target location.
     */
    public int moveTo(Location location) {
        if (mTurnRemainingMoves == 0) {
            throw new IllegalStateException("No more moves remaining");
        }

        if (location == mTurnInitialPlayerRoom) {
            throw new IllegalArgumentException("Cannot return to room player started in");
        }

        // Make sure tile can be reached within the allowed number of moves
        List<TokenOccupiableTile> shortestPath = PathFinder.findShortestPathAdvanced(mCurrentPlayerLocation, location, mTurnRemainingMoves);
        if (shortestPath == null) {
            throw new IllegalArgumentException("Cannot move to location " + location);
        }

        // Subtract the number of moves used
        mTurnRemainingMoves -= shortestPath.size() - 1;

        if (!mCurrentPlayerLocation.isRoom()) {
            shortestPath.remove(0);
        }
        if (!location.isRoom()) {
            shortestPath.remove(shortestPath.size() - 1);
        }

        // Update states
        completeMove(location, shortestPath);

        return mTurnRemainingMoves;
    }

    /**
     * Stops movement before all moves have been used and allows turn to end.
     *
     * @throws IllegalStateException If the player has not completed at least one move, or attempts to stop movement in a room.
     */
    public void stopMoving() {
        // Ensure one move has been performed
        if (!mTurnHasMoved) {
            throw new IllegalStateException("Must complete at least one move before stopping");
        }

        // Stopping movement is only relevant in corridors (unusual case)
        if (mCurrentPlayerLocation.isRoom()) {
            throw new IllegalStateException("Player cannot stop movement once already in room");
        }

        // Turn is finished
        mTurnRemainingMoves = 0;
        mTurnFinished = true;
    }

    /**
     * Completes movement to a new {@code Location} and updates states.
     *
     * @param location the player's new location
     * @param path the path taken to the new location
     */
    private void completeMove(Location location, List<? extends Tile> path) {
        // Update whether the player has moved to allow stopping movement before all moves get used up
        if (location != mCurrentPlayerLocation) {
            mTurnHasMoved = true;
        }

        // Set new location
        mCurrentPlayer.getCharacter().setLocation(location, path);
        mCurrentPlayerLocation = location;

        // Update states
        if (location.isRoom()) {
            mTurnCanPoseQuestion = !location.asRoom().isGuessRoom();
            mTurnCanMakeFinalAccusation = location.asRoom().isGuessRoom();
            mTurnRemainingMoves = 0;
        } else if (mTurnRemainingMoves <= 0) {
            mTurnFinished = true;
        }
    }

    /**
     * Poses a question to other players and returns the first player that has a matching card.
     *
     * @param suggestion Question to pose.
     * @return The first player that has a card contained in the suggestion.
     * @throws IllegalStateException If the player is not in a room or has not moved out of their previous room.
     */
    public Player poseQuestion(Suggestion suggestion) {
        // Can't pose question unless allowed
        if (!mTurnCanPoseQuestion) {
            throw new IllegalStateException("Player cannot make guess");
        }

        // Can't pose question using undistributed cards
        if (mUndistributedCards.contains(suggestion.suspect) || mUndistributedCards.contains(suggestion.weapon)) {
            throw new IllegalArgumentException("Cannot make guess using any of the undistributed cards");
        }

        // Can no longer pose question
        mTurnCanPoseQuestion = false;

        // Can no longer move
        mTurnMovementComplete = true;

        // Check whether the suspect is being moved (to allow for guess without movement at next turn)
        if (!suggestion.suspect.getMovedSinceLastTurn()) {
            suggestion.suspect.setMovedSinceLastTurn(suggestion.suspect.getLocation() != suggestion.room);
        }

        // Move the suspect and weapon in question to the room
        suggestion.suspect.setLocation(suggestion.room, null);
        suggestion.weapon.setLocation(suggestion.room, null);

        // Loop through players
        Player matchingPlayer = null;
        Iterator<Player> iterator = mPlayers.iteratorStartingAfter(mCurrentPlayer);
        while (iterator.hasNext()) {
            Player checkPlayer = iterator.next();
            // Return first player that has one of the suggestion's cards
            if (checkPlayer.hasAnySuggestionCards(suggestion)) {
                matchingPlayer = checkPlayer;
                break;
            }

            for (Card card : suggestion.asList()) {
                for (Player player : mPlayers) {
                    if (player == checkPlayer) continue;
                    player.getKnowledge().setHolding(card, checkPlayer, false);
                }
            }
        }

        // If no matches found, either correct final accusation or player posing question has cards
        if (matchingPlayer == null) {
            // Insert log entry
            mLog.add(LogEntry.newQuestionEntry(mCurrentPlayer, suggestion, null, null));

            // Turn can finish as no response is required
            mTurnFinished = true;

            return null;
        }

        mTurnQuestionSuggestion = suggestion;
        mTurnQuestionCardHolder = matchingPlayer;

        return matchingPlayer;
    }

    /**
     * Acknowledges the response of a player who had a card in a previously asked question.
     *
     * @param card Response card.
     */
    public void questionResponse(Card card) {
        if (mTurnQuestionCardHolder == null || mTurnQuestionSuggestion == null) {
            throw new IllegalStateException("Not waiting for a question response");
        }

        if (!mTurnQuestionCardHolder.hasCard(card)) {
            throw new IllegalArgumentException("Player " + mTurnQuestionCardHolder.getName() + " does not have " + card.getName());
        }

        if (!mTurnQuestionSuggestion.asList().contains(card)) {
            throw new IllegalArgumentException(card.getName() + " is not one of the suggested cards");
        }

        mCurrentPlayer.getKnowledge().setHolding(card, mTurnQuestionCardHolder, true);

        // Insert log entry
        mLog.add(LogEntry.newQuestionEntry(mCurrentPlayer, mTurnQuestionSuggestion, mTurnQuestionCardHolder, card));

        mTurnQuestionSuggestion = null;
        mTurnQuestionCardHolder = null;

        mTurnFinished = true;
    }

    /**
     * Makes the final accusation and returns {@code true} if the accusation is correct.
     *
     * The player must be in the guess room to make the final accusation.
     *
     * If the accusation is correct, the game is finished, otherwise the player is eliminated.
     *
     * @param suggestion Final accusation.
     * @return {@code true} if the accusation is correct.
     * @throws IllegalStateException If the player is not currently in the guess room.
     */
    public boolean makeFinalAccusation(Suggestion suggestion) {
        // Can't make accusation outside of guess room
        if (!mTurnCanMakeFinalAccusation) {
            throw new IllegalStateException("Player is not in guess room");
        }

        // Turn is finished once an accusation is made
        mTurnFinished = true;

        // Can no longer make final accusation
        mTurnCanMakeFinalAccusation = false;

        // Insert log entry
        mLog.add(LogEntry.newFinalAccusationEntry(mCurrentPlayer, suggestion, mSolution.equals(suggestion)));

        // Check whether accusation is correct
        if (mSolution.equals(suggestion)) {
            mFinished = true;
            return true;
        } else {
            // Remove player if accusation is incorrect
            mActivePlayerIterator.remove();
            mFinished = mActivePlayers.size() <= 1;
            return false;
        }
    }

    /**
     * Returns the player's current location.
     *
     * @return the player's current location.
     */
    public Location getCurrentPlayerLocation() {
        return mCurrentPlayerLocation;
    }

    /**
     * Returns the {@code Room} in which the player was at the beginning of their turn.
     *
     * Used to ensure that the player does not return to the same room during their turn.
     *
     * @return the {@code Room} in which the player was at the beginning of their turn.
     */
    public Location getTurnInitialPlayerRoom() {
        return mTurnInitialPlayerRoom;
    }

    /**
     * Returns {@code true} if the player is currently able to roll the dice for movement.
     *
     * @return {@code true} if the player is currently able to roll the dice for movement.
     */
    public boolean canRollDice() {
        return !mTurnMovementComplete && mTurnCanRollDice;
    }

    /**
     * Returns {@code true} if the player is currently able to use a secret passage.
     *
     * @return {@code true} if the player is currently able to use a secret passage.
     */
    public boolean canUsePassage() {
        return !mTurnMovementComplete && mTurnCanUsePassage;
    }

    /**
     * Returns the number of moves remaining in the player's turn.
     *
     * @return the number of moves remaining in the player's turn.
     */
    public int getTurnRemainingMoves() {
        return mTurnRemainingMoves;
    }

    /**
     * Returns {@code true} if the player is currently able to pose a question.
     *
     * @return {@code true} if the player is currently able to pose a question.
     */
    public boolean canPoseQuestion() {
        return mTurnCanPoseQuestion;
    }

    /**
     * Returns {@code true} if the player is currently able to make a final accusation.
     *
     * @return {@code true} if the player is currently able to make a final accusation.
     */
    public boolean canMakeFinalAccusation() {
        return mTurnCanMakeFinalAccusation;
    }

    /**
     * Returns {@code true} if the player is currently able to stop moving.
     *
     * @return {@code true} if the player is currently able to stop moving.
     */
    public boolean canStopMoving() {
        return mTurnHasMoved && mTurnRemainingMoves > 0;
    }

    /**
     * Returns {@code true} if the player's turn is finished.
     *
     * @return {@code true} if the player's turn is finished.
     */
    public boolean isTurnFinished() {
        return mTurnFinished;
    }

    /**
     * Returns {@code true} if the game has finished.
     *
     * The game has finished once a correct accusation has been made or all players have made incorrect accusations.
     *
     * @return {@code true} if the game has finished.
     */
    public boolean isFinished() {
        return mFinished;
    }

    /**
     * Chooses a random solution and distributes remaining cards to players.
     *
     * Once the solution has been chosen the remaining cards are evenly distributed to players,
     * any cards that cannot be evenly distributed are placed in the undistributed cards pile,
     * which is visible to all players.
     */
    private void setupCards() {
        // Create a random solution
        mSolution = new Suggestion(randomCard(mBoard.getSuspects()),
                randomCard(mBoard.getWeapons()),
                randomCard(mBoard.getRooms()));

        // Cards to be distributed to players
        List<Card> distributeCards = mBoard.getCardsModifiable();

        // Remove all solution cards
        distributeCards.removeAll(mSolution.asList());

        // Shuffle cards
        Collections.shuffle(distributeCards);

        // Place cards that will not divide evenly into the undistributed cards pile
        mUndistributedCards = distributeCards.stream()
                .limit(distributeCards.size() % mPlayers.size())
                .collect(Collectors.toList());

        // Remove undistributed cards from cards to be distributed
        distributeCards.removeAll(mUndistributedCards);

        // Distribute cards to players
        for (int i = 0; i < distributeCards.size(); i++) {
            mPlayers.get(i % mPlayers.size()).addCard(distributeCards.get(i));
        }

        // Initiate player card knowledge
        for (Player player : mPlayers) {
            player.initiateKnowledge(mBoard.getCards(), mPlayers, mUndistributedCards);
        }
    }

    private static <C extends Card> C randomCard(List<C> list) {
        return list.get(sRandom.nextInt(list.size()));
    }
}
