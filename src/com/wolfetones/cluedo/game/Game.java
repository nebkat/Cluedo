package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.*;
import java.util.List;

public class Game {
	private static Random sRandom = new Random();

	private boolean mStarted = false;
	private boolean mFinished = false;

	private List<Card> mCards = new ArrayList<>(21);
	private List<Card> mRemainingCards = new ArrayList<>(2);

	private Suggestion mSolution;

	private List<Player> mPlayers = new ArrayList<>(6);
	private List<Player> mActivePlayers;
	private ListIterator<Player> mActivePlayerIterator;
	private Player mCurrentPlayer;
	private Location mCurrentPlayerLocation;

	private boolean mTurnCanRollDice;
	private boolean mTurnCanUsePassage;
    private boolean mTurnCanPoseQuestion;
    private boolean mTurnCanMakeFinalAccusation;
    private boolean mTurnFinished;
	private int mTurnRemainingMoves;
	private boolean mTurnHasMoved;

	private boolean mTurnMovementComplete;

	private BoardModel mBoard = new BoardModel();

    public Game() {}

    public void addPlayer(Player player) {
	    if (mStarted) {
	        throw new IllegalStateException("Players cannot be added once game has begun");
        }

	    mPlayers.add(player);
    }

    public void start() {
	    mStarted = true;

	    mActivePlayers = new ArrayList<>(mPlayers);
	    mActivePlayerIterator = mActivePlayers.listIterator();

	    setupCards();
    }

    public boolean isFinished() {
        return mFinished;
    }

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
        completeMove(((Room) mCurrentPlayerLocation).getPassageRoom());

        // Can no longer move
        mTurnMovementComplete = true;
    }

    public void rollDice(int[] dice) {
        // Can't roll dice if player has already moved this turn
        if (mTurnMovementComplete) {
            throw new IllegalStateException("Player has already moved");
        }

        // Can't roll dice if nowhere to move to
        if (!mTurnCanRollDice) {
            throw new IllegalStateException("Player has nowhere to move to");
        }

        // Dice array must be of size 2
        if (dice.length != 2) {
            throw new IllegalArgumentException("int[] dice must be of size 2");
        }
        // Random dice roll
        dice[0] = sRandom.nextInt(60) + 1;
        dice[1] = sRandom.nextInt(60) + 1;
        mTurnRemainingMoves = dice[0] + dice[1];

        // Can no longer move
        mTurnMovementComplete = true;

        // Can no longer guess (until movement is complete)
        mTurnCanPoseQuestion = false;
    }

    public int moveTo(Location location) {
        if (mTurnRemainingMoves == 0) {
            throw new InvalidStateException("No more moves remaining");
        }

        // Make sure tile can be reached within the allowed number of moves
        List<TokenOccupiableTile> shortestPath = PathFinder.findShortestPathAdvanced(mCurrentPlayerLocation, location, mTurnRemainingMoves);
        if (shortestPath == null) {
            throw new InvalidStateException("Cannot move to location " + location);
        }

        mTurnRemainingMoves -= shortestPath.size() - 1;

        completeMove(location);

        return mTurnRemainingMoves;
    }

    public void stopMoving() {
        if (!mTurnHasMoved) {
            throw new IllegalStateException("Must complete at least one move before stopping");
        }

        mTurnRemainingMoves = 0;
        completeMove(mCurrentPlayerLocation);
    }

    private void completeMove(Location newLocation) {
        if (newLocation != mCurrentPlayerLocation) {
            mTurnHasMoved = true;
        }

        mCurrentPlayer.getCharacter().setLocation(newLocation);
        mCurrentPlayerLocation = newLocation;

        if (newLocation.isRoom()) {
            mTurnCanPoseQuestion = !newLocation.asRoom().isGuessRoom();
            mTurnCanMakeFinalAccusation = newLocation.asRoom().isGuessRoom();
            mTurnRemainingMoves = 0;
        } else if (mTurnRemainingMoves <= 0) {
            mTurnFinished = true;
        }
    }

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

    private void setupCards() {
        mCards.addAll(mBoard.getRooms());
        mCards.addAll(mBoard.getSuspects());
        mCards.addAll(mBoard.getWeapons());

        mSolution = new Suggestion(randomCard(mBoard.getRooms()),
                randomCard(mBoard.getSuspects()),
                randomCard(mBoard.getWeapons()));

        List<Card> distributeCards = new ArrayList<>(mCards);
        distributeCards.removeAll(mSolution.asList());
        Collections.shuffle(distributeCards);

        for (int i = 0; i < distributeCards.size() % mPlayers.size(); i++) {
            mRemainingCards.add(distributeCards.remove(0));
        }

        for (int i = 0; i < distributeCards.size(); i++) {
            mPlayers.get(i % mPlayers.size()).addCard(distributeCards.remove(0));
        }
    }

    private <C extends Card> C randomCard(List<C> list) {
		return list.get(sRandom.nextInt(list.size()));
	}
}
