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

import java.util.*;
import java.util.List;

public class Game {
    private static final int NUM_DICE = 2;

	private static Random sRandom = new Random();

    /**
     * State
     */
	private boolean mStarted = false;
	private boolean mFinished = false;

    /**
     * Cards
     */
	private List<Card> mCards = new ArrayList<>(21);
	private List<Card> mRemainingCards = new ArrayList<>(2);

    /**
     * Game solution
     */
	private Suggestion mSolution;

    /**
     * Players
     */
	private List<Player> mPlayers = new ArrayList<>(6);
	private List<Player> mActivePlayers;
	private ListIterator<Player> mActivePlayerIterator;
	private Player mCurrentPlayer;
	private Location mCurrentPlayerLocation;

    /**
     * Current abilities in turn, depending on state and location
     */
	private boolean mTurnCanRollDice;
	private boolean mTurnCanUsePassage;
    private boolean mTurnCanPoseQuestion;
    private boolean mTurnCanMakeFinalAccusation;
    private boolean mTurnFinished;
	private int mTurnRemainingMoves;
	private boolean mTurnHasMoved;

	private boolean mTurnMovementComplete;

    /**
     * Board
     */
	private BoardModel mBoard = new BoardModel();

    public Game() {}

    /**
     * Add a player to the game.
     *
     * Must be performed before game has started.
     *
     * @param player Player to add to the game.
     */
    public void addPlayer(Player player) {
	    if (mStarted) {
	        throw new IllegalStateException("Players cannot be added once game has started");
        }

	    mPlayers.add(player);
    }

    /**
     * Start the game.
     */
    public void start() {
        if (mStarted) {
            throw new IllegalStateException("Game already started");
        }

	    mStarted = true;

	    mActivePlayers = new ArrayList<>(mPlayers);
	    mActivePlayerIterator = mActivePlayers.listIterator();

	    setupCards();
    }

    /**
     * Returns {@code true} if the game has finished.
     *
     * The game has finished once a correct accusation has been made or all
     * players have made incorrect accusations.
     *
     * @return {@code true} if the game has finished.
     */
    public boolean isFinished() {
        return mFinished;
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

        // Check if the player is capable of performing a move
        if (mCurrentPlayerLocation.isRoom()) {
            mTurnCanUsePassage = mCurrentPlayerLocation.asRoom().hasPassage();
            mTurnCanRollDice = mCurrentPlayerLocation.asRoom()
                    .getEntranceCorridors()
                    .stream()
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

        // Check if player can perform anything (rare case that player is stuck in corridor)
        mTurnFinished = mTurnMovementComplete && !mTurnCanPoseQuestion;

        // Reset movement variables
        mTurnHasMoved = false;
        mTurnRemainingMoves = 0;

        return mCurrentPlayer;
    }

    public boolean canRollDice() {
        return !mTurnMovementComplete && mTurnCanRollDice;
    }

    public boolean canUsePassage() {
        return !mTurnMovementComplete && mTurnCanUsePassage;
    }

    public boolean canPoseQuestion() {
        return mTurnCanPoseQuestion;
    }

    public boolean canMakeFinalAccusation() {
        return mTurnCanMakeFinalAccusation;
    }

    public boolean isTurnFinished() {
        return mTurnFinished;
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

        // Move to new room
        completeMove(mCurrentPlayerLocation.asRoom().getPassageRoom());

        // Can no longer move
        mTurnMovementComplete = true;
    }

    /**
     * Rolls the dice randomly and returns the number of allowed moves.
     *
     * @param dice Array of size {@value NUM_DICE} to store individual dice values.
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
            dice[i] = sRandom.nextInt(6) + 1;
            mTurnRemainingMoves += dice[i];
        }

        // Can no longer move
        mTurnMovementComplete = true;

        // Can no longer guess (until movement is complete)
        mTurnCanPoseQuestion = false;

        return mTurnRemainingMoves;
    }

    /**
     * Moves the player's {@code Token} to a {@link Location} and subtracts the number of moves required.
     *
     * @param location Location to move to.
     * @return The number of moves remaining.
     * @throws IllegalStateException If there are not enough moves available or there is no path to the target location.
     */
    public int moveTo(Location location) {
        if (mTurnRemainingMoves == 0) {
            throw new IllegalStateException("No more moves remaining");
        }

        // Make sure tile can be reached within the allowed number of moves
        List<TokenOccupiableTile> shortestPath = PathFinder.findShortestPathAdvanced(mCurrentPlayerLocation, location, mTurnRemainingMoves);
        if (shortestPath == null) {
            throw new IllegalStateException("Cannot move to location " + location);
        }

        // Subtract the number of moves used
        mTurnRemainingMoves -= shortestPath.size() - 1;

        // Update states
        completeMove(location);

        return mTurnRemainingMoves;
    }

    /**
     * Stops movement before all moves have been used and allows turn to end.
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
     * @param newLocation New location.
     */
    private void completeMove(Location newLocation) {
        // Update whether the player has moved to allow stopping movement before all moves get used up
        if (newLocation != mCurrentPlayerLocation) {
            mTurnHasMoved = true;
        }

        // Set new location
        mCurrentPlayer.getCharacter().setLocation(newLocation);
        mCurrentPlayerLocation = newLocation;

        // Update states
        if (newLocation.isRoom()) {
            mTurnCanPoseQuestion = !newLocation.asRoom().isGuessRoom();
            mTurnCanMakeFinalAccusation = newLocation.asRoom().isGuessRoom();
            mTurnRemainingMoves = 0;
        } else if (mTurnRemainingMoves <= 0) {
            mTurnFinished = true;
        }
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

        // Check whether accusation is correct
        if (mSolution.equals(suggestion)) {
            mFinished = true;
            return true;
        } else {
            // Remove player if accusation is incorrect
            mActivePlayerIterator.remove();
            mFinished = mActivePlayers.isEmpty();
            return false;
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

        // Player can finish turn once a guess has been made
        mTurnFinished = true;

        // Can no longer pose question
        mTurnCanPoseQuestion = false;

        // Can no longer move
        mTurnMovementComplete = true;

        // Check whether the suspect is being moved (to allow for guess without movement at next turn)
        suggestion.suspect.setMovedSinceLastTurn(suggestion.suspect.getLocation() != suggestion.room);

        // Move the suspect and weapon in question to the room
        suggestion.suspect.setLocation(suggestion.room);
        suggestion.weapon.setLocation(suggestion.room);

        // Loop through players
        ListIterator<Player> iterator = mPlayers.listIterator((mPlayers.indexOf(mCurrentPlayer) + 1) % mPlayers.size());
        Player checkPlayer;
        while ((checkPlayer = iterator.next()) != mCurrentPlayer) {
            // Return first player that has one of the suggestion's cards
            if (checkPlayer.hasAnySuggestionCards(suggestion)) {
                return checkPlayer;
            }

            // Loop around to first player
            if (!iterator.hasNext()) {
                iterator = mPlayers.listIterator();
            }
        }

        // No matches found, either correct final accusation or player posing questions has cards
        return null;
    }

    public BoardModel getBoard() {
	    return mBoard;
    }

    public Player getCurrentPlayer() {
        return mCurrentPlayer;
    }

    public Location getCurrentPlayerLocation() {
        return mCurrentPlayerLocation;
    }

    public Suggestion getSolution() {
        return mSolution;
    }

    /**
     * Chooses a random solution and distributes remaining cards to players
     *
     * Once the solution has been chosen the remaining cards are evenly distributed to players,
     * Any cards that cannot be evenly distributed are placed in the remaining cards pile, which
     * is visible to all players.
     */
    private void setupCards() {
        // Add all cards to the cards list
        mCards.addAll(mBoard.getRooms());
        mCards.addAll(mBoard.getSuspects());
        mCards.addAll(mBoard.getWeapons());

        // Create a random solution
        mSolution = new Suggestion(randomCard(mBoard.getRooms()),
                randomCard(mBoard.getSuspects()),
                randomCard(mBoard.getWeapons()));

        // Cards to be distributed to players
        List<Card> distributeCards = new ArrayList<>(mCards);

        // Remove all solution cards
        distributeCards.removeAll(mSolution.asList());

        // Shuffle cards
        Collections.shuffle(distributeCards);

        // Place cards that will not divide evenly into the remaining cards pile
        for (int i = 0; i < distributeCards.size() % mPlayers.size(); i++) {
            mRemainingCards.add(distributeCards.remove(0));
        }

        // Distribute cards to players
        for (int i = 0; i < distributeCards.size(); i++) {
            mPlayers.get(i % mPlayers.size()).addCard(distributeCards.remove(0));
        }
    }

    private <C extends Card> C randomCard(List<C> list) {
		return list.get(sRandom.nextInt(list.size()));
	}
}
