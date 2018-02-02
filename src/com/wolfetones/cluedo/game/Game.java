package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;

import java.util.*;
import java.util.List;

public class Game {
	private static Random sRandom = new Random();

	private boolean mStarted = false;

	private List<Card> mCards = new ArrayList<>(21);
	private List<Card> mRemainingCards;

	private Suggestion mSolution;

	private List<Player> mPlayers = new ArrayList<>(6);
	private int mCurrentPlayerIndex = -1;
	private Player mCurrentPlayer;
	private TokenOccupiableTile mCurrentPlayerTile;

	private boolean mMoveMovementsCompleted;
	private int mRemainingMovements;
	private boolean mCanMakeGuess;

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

	    setupCards();
    }

    public Player nextMove() {
        mCurrentPlayerIndex++;

        if (mCurrentPlayerIndex >= mPlayers.size()) {
            mCurrentPlayerIndex = 0;
        }

        mCurrentPlayer = mPlayers.get(mCurrentPlayerIndex);
        mCurrentPlayerTile = mCurrentPlayer.getCharacter().getTile();

        mMoveMovementsCompleted = false;
        mRemainingMovements = 0;
        mCanMakeGuess = false;

        return mCurrentPlayer;
    }

    public boolean usePassage() {
        if (mMoveMovementsCompleted) {
            System.err.println("Player has already moved");
            return false;
        }

        if (!(mCurrentPlayerTile instanceof RoomTile)) {
            System.err.println("Player is not in a room");
            return false;
        }

        if (!(((RoomTile) mCurrentPlayerTile).getRoom()).hasPassage()) {
            System.err.println("Room that player is in does not have a secret passage");
            return false;
        }

        RoomTile newTile = ((RoomTile) mCurrentPlayerTile).getRoom().getPassageRoom().getNextUnoccupiedTile();
        mCurrentPlayer.getCharacter().setTile(newTile);
        mCurrentPlayerTile = newTile;

        mMoveMovementsCompleted = true;

        return true;
    }

    public int rollDice() {
        if (mMoveMovementsCompleted) {
            System.err.println("Player has already moved");
            return -1;
        }

        mRemainingMovements = sRandom.nextInt(30 - 1) + 2;

        mMoveMovementsCompleted = true;

        return mRemainingMovements;
    }

    public BoardModel getBoard() {
	    return mBoard;
    }

    public Player getCurrentPlayer() {
        return mCurrentPlayer;
    }

    public TokenOccupiableTile getCurrentPlayerTile() {
        return mCurrentPlayerTile;
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

        for (int i = 0; i < distributeCards.size() / mPlayers.size(); i++) {
            mPlayers.get(i % mPlayers.size()).addCard(distributeCards.remove(0));
        }

        mRemainingCards = distributeCards;
    }

    private <C extends Card> C randomCard(List<C> list) {
		return list.get(sRandom.nextInt(list.size()));
	}
}
