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

package bots;

import gameengine.*;

import java.util.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WolfeTones implements BotAPI {
	private static final boolean CAN_QUESTION_IN_NEW_ROOMS = false;

	private static Random sRandom = new Random();

	private static final int AVERAGE_ROLL = 7;

	private static List<String> SUSPECTS = Arrays.asList(Names.SUSPECT_NAMES);
	private static List<String> WEAPONS = Arrays.asList(Names.WEAPON_NAMES);
	private static List<String> ROOMS = Arrays.asList(Names.ROOM_CARD_NAMES);
	private static List<String> CARDS = new ArrayList<>() {{
		addAll(SUSPECTS);
		addAll(WEAPONS);
		addAll(ROOMS);
	}};
	private static String GUESS_ROOM = Names.ROOM_NAMES[Names.ROOM_NAMES.length - 1];
	private static List<List<String>> CARD_LISTS = Arrays.asList(SUSPECTS, WEAPONS, ROOMS);

	private static final String COMMAND_ROLL = "roll";
	private static final String COMMAND_PASSAGE = "passage";
	private static final String COMMAND_DONE = "done";
	private static final String COMMAND_QUESTION = "question";
	private static final String COMMAND_ACCUSE = "accuse";

	private static final String DIRECTION_LEFT = "l";
	private static final String DIRECTION_UP = "u";
	private static final String DIRECTION_RIGHT = "r";
	private static final String DIRECTION_DOWN = "d";

	private static final String[] DIRECTIONS = {DIRECTION_LEFT, DIRECTION_UP, DIRECTION_RIGHT, DIRECTION_DOWN};

	private static final String LOG_QUESTION_REGEX = "(.*) \\((.*)\\) questioned (.*) \\((.*)\\) about (" + java.lang.String.join("|", SUSPECTS) + ") with the (" + java.lang.String.join("|", WEAPONS) + ") in the (" + java.lang.String.join("|", ROOMS) + ").";
	private static final String LOG_ANSWER_REGEX = "(.*) \\(.*\\) (did not show any cards|showed one card)(?:: (.*))?.";
	private static final Pattern LOG_QUESTION_PATTERN = Pattern.compile(LOG_QUESTION_REGEX);
	private static final Pattern LOG_ANSWER_PATTERN = Pattern.compile(LOG_ANSWER_REGEX);
	private static final String LOG_SHOWED_CARD = "showed one card";

	private String mName;
	private Player mPlayer;
	private Token mToken;
	private String mCharacter;
	private PlayersInfo mPlayersInfo;
	private gameengine.Map mMap;
	private Dice mDice;
	private Log mLog;
	private Deck mDeck;

	private List<String> mOtherPlayers = new ArrayList<>();
	private Set<String> mOtherPlayerSuspects = new HashSet<>();

	private List<String> mPlayerCards = new ArrayList<>();
	private List<String> mPlayerSuspectCards;
	private List<String> mPlayerWeaponCards;
	private List<String> mPlayerRoomCards;
	private int mPlayerNumCards;

	private int mLogCurrentIndex;

	private boolean mTest;

	private boolean mInitialized;

	private KnowledgeMap mKnowledge;
	private PathFinder mPathFinder = new PathFinder();
	private Map<String, Set<String>> mShownCards = new HashMap<>();

    public WolfeTones(Player player, PlayersInfo playersInfo, gameengine.Map map, Dice dice, Log log, Deck deck) {
    	mName = getClass().getSimpleName();
    	mPlayer = player;
    	mToken = player.getToken();
    	mCharacter = player.getToken().getName();
    	mPlayersInfo = playersInfo;
    	mMap = map;
    	mDice = dice;
    	mLog = log;
    	mDeck = deck;

    	mTest = getClass() == WolfeTones.class;
    }

	private void initialize() {
		// Players
		mOtherPlayers = new ArrayList<>(Arrays.asList(mPlayersInfo.getPlayersNames()));
		mOtherPlayers.remove(mName);

		// Cards
		mPlayerCards = CARDS.stream().filter(mPlayer::hasCard).collect(Collectors.toList());
		mPlayerNumCards = (CARDS.size() - 3) / mPlayersInfo.numPlayers();
		mPlayerSuspectCards = SUSPECTS.stream().filter(mPlayer::hasCard).collect(Collectors.toList());
		mPlayerWeaponCards = WEAPONS.stream().filter(mPlayer::hasCard).collect(Collectors.toList());
		mPlayerRoomCards = ROOMS.stream().filter(mPlayer::hasCard).collect(Collectors.toList());

		// Knowledge
		mKnowledge = new KnowledgeMap();

		mInitialized = true;
	}

    public String getName() {
        return mName;
    }

	@Override
	public String getVersion() {
		return "32c";
	}

	private boolean mWaitingForTurn = true;
	private boolean mTurnUsePassage = false;
	private boolean mTurnMovementComplete = false;
	private boolean mTurnHasAskedQuestion = false;
	private String mTurnFinishRoom = "";

	private String mTargetRoom = null;

	private boolean mWaitingForMovement = true;
	private String mMovementPath;
	private int mMovementPathIndex;

    public String getCommand() {
    	// Initialize if not initialized yet
    	if (!mInitialized) initialize();

    	// Parse new log lines
    	parseLog();

    	// Turn start
    	if (mWaitingForTurn) turnStart();

		if (!mTurnMovementComplete) {
			mTurnMovementComplete = true;
			return mTurnUsePassage ? COMMAND_PASSAGE : COMMAND_ROLL;
		}

    	if (isInRoom()) {
    		// If entered guess room, must make final accusation
    		if (getCurrentRoom().equals(GUESS_ROOM)) {
    			return COMMAND_ACCUSE;
			}

			if (!mTurnHasAskedQuestion && getCurrentRoom().equals(mTargetRoom)) {
				mTurnHasAskedQuestion = true;
				mTurnMovementComplete = true;
				return COMMAND_QUESTION;
			}
		}

		// Turn end
		turnEnd();
    	return COMMAND_DONE;
    }

    private void turnStart() {
    	// Can ask question immediately if moved since last turn
		boolean turnCanAskQuestion = CAN_QUESTION_IN_NEW_ROOMS && isInRoom() && !mTurnFinishRoom.equals(getCurrentRoom());

		String passageRoom = isInRoom() && mToken.getRoom().getPassageDestination() != null ?
				mToken.getRoom().getPassageDestination().toString() : "";

		if (mKnowledge.getCorrectCount() == 3) {
			// Go straight to guess room if all 3 known
			mTargetRoom = GUESS_ROOM;
		} else if (turnCanAskQuestion && mKnowledge.getStatus(getCurrentRoom()).isUnknown()) {
			// Stay in current room and ask again if current room unknown and question allowed
			mTargetRoom = getCurrentRoom();
		} else {
			// Move to closest unknown room
			mTargetRoom = mKnowledge.getStatuses(ROOMS).stream()
					.filter(KnowledgeMap.CardStatus::isUnknown)
					.map(KnowledgeMap.CardStatus::getCard)
					.filter(r -> !getCurrentRoom().equals(r)) // Can't ask in current room
					.min(Comparator.comparingInt(room -> {
						if (room.equals(passageRoom)) {
							// Always prefer passage room
							return 0;
						} else if (room.equals(mTurnFinishRoom)) {
							// Try avoid asking in same room repeatedly
							return Integer.MAX_VALUE;
						}
						return mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(room)).size();
					})).orElse(null);

			// No unknown rooms remaining (except potentially current room)
			if (mTargetRoom == null) {
				if (mKnowledge.getCorrectRoom() != null && !mKnowledge.getCorrectRoom().equals(getCurrentRoom())) {
					// Go to one of held rooms or correct room to try determine suspect/weapon if rooms are known
					List<String> potentialRooms = new ArrayList<>(mPlayerRoomCards);
					potentialRooms.add(mKnowledge.getCorrectRoom());

					// Go to room closest to guess room as answer will probably be known soon
					mTargetRoom = potentialRooms.stream()
							.filter(r -> !getCurrentRoom().equals(r)) // Can't ask in current room
							.sorted(Comparator.comparingInt(room -> {
								if (room.equals(passageRoom)) {
									return 0;
								}
								return mPathFinder.findShortestPathAdvanced(mMap.getRoom(room), mMap.getRoom(GUESS_ROOM)).size();
							}))
							.findAny().get();
				} else {
					// Go to closest room
					mTargetRoom = ROOMS.stream()
							.filter(r -> !getCurrentRoom().equals(r)) // Can't ask in current room
							.min(Comparator.comparingInt(room -> {
								if (room.equals(passageRoom)) {
									// Always prefer passage room
									return 0;
								}
								return mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(room)).size();
							})).get();
				}
			}
		}

		if (!passageRoom.isEmpty()) {
			// Current room has passage
			if (mTargetRoom.equals(passageRoom)) {
				// Target room can be reached directly using passage
				mTurnUsePassage = true;
			} else {
				int directRoute = mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(mTargetRoom)).size() - AVERAGE_ROLL;
				int passageRoute = mPathFinder.findShortestPathAdvanced(mMap.getRoom(passageRoom), mMap.getRoom(mTargetRoom)).size();

				// Target room is quicker to reach through passage
				if (passageRoute < directRoute) {
					mTurnUsePassage = true;
				}
			}
		} else {
			Room targetRoom = mMap.getRoom(mTargetRoom);
			String targetRoomPassageRoom = targetRoom.getPassageDestination() != null ? targetRoom.getPassageDestination().toString() : "";
			// Target room has passage
			if (!targetRoomPassageRoom.isEmpty()) {
				int directRoute = mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(mTargetRoom)).size() - AVERAGE_ROLL;
				int passageRoute = mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(targetRoomPassageRoom)).size();

				// Target room is quicker to reach through passage
				if (passageRoute < directRoute) {
					mTargetRoom = targetRoomPassageRoom;
				}
			}
		}

		// No longer waiting for next turn
		mWaitingForTurn = false;
	}

	private void turnEnd() {
    	// Reset states
		mTurnMovementComplete = false;
		mTurnHasAskedQuestion = false;
		mTurnUsePassage = false;

		// Store room we ended in
		mTurnFinishRoom = getCurrentRoom();

		// Waiting for movement
		mWaitingForMovement = true;

		// Waiting for next turn
    	mWaitingForTurn = true;
	}

	private void movementStart() {
    	int diceRoll = mDice.getTotal();

		List<Tile> targetPath = mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(mTargetRoom));
		// Room can't be reached in one turn
    	if (targetPath.size() - 1 > diceRoll) {
    		// See if there is a room that is a shortcut to the target room
			for (String room : ROOMS) {
				if (room.equals(mTargetRoom)) continue;

				// Distance to room
				List<Tile> pathToRoom = mPathFinder.findShortestPathAdvanced(getCurrentLocation(), mMap.getRoom(room));
				if (pathToRoom.size() - 1 > diceRoll) continue;

				// Distance from room
				List<Tile> pathFromRoom = mPathFinder.findShortestPathAdvanced(mMap.getRoom(room), mMap.getRoom(mTargetRoom));
				if (pathFromRoom.size() < targetPath.size() - diceRoll) {
					targetPath = pathToRoom;
				}
			}
		}

		mMovementPath = mPathFinder.tilePathToMovementString(targetPath);

		if (diceRoll < mMovementPath.length()) {
			mMovementPath = mMovementPath.substring(0, diceRoll);
		}

		mMovementPathIndex = 0;

    	// No longer waiting for movement
		mWaitingForMovement = false;
	}

	private void movementEnd() {
    	mMovementPath = null;

    	// Waiting for next movement
		mWaitingForMovement = true;
	}

	private boolean isInRoom() {
    	return mToken.isInRoom();
	}

	private Object getCurrentLocation() {
    	if (isInRoom()) {
    		return mToken.getRoom();
		} else {
    		return new Tile(mToken.getPosition());
		}
	}

	private String getCurrentRoom() {
    	return mToken.getRoom() == null ? "" : mToken.getRoom().toString();
	}

    public String getMove() {
    	if (mWaitingForMovement) movementStart();

    	String move = Character.toString(mMovementPath.charAt(mMovementPathIndex++));

    	if (mMovementPathIndex == mMovementPath.length()) movementEnd();
        return move;
    }

    private int mQuestionCount = 0;

    private static long sQuestionCountTotal = 0;
    private static int sIterations = 0;
    private static int sTestCount = 0;

	@Override
    public String getSuspect() {
		if (getCurrentRoom().equals(GUESS_ROOM)) {
			return mKnowledge.getCorrectSuspect();
		}

		mQuestionCount++;

        return getCardFrom(SUSPECTS, mKnowledge.getCorrectSuspect());
    }

	@Override
    public String getWeapon() {
    	if (getCurrentRoom().equals(GUESS_ROOM)) {
    		return mKnowledge.getCorrectWeapon();
		}

		return getCardFrom(WEAPONS, mKnowledge.getCorrectWeapon());
    }

	@Override
    public String getRoom() {
		sQuestionCountTotal += mQuestionCount;
		sIterations++;
		if (mTest) sTestCount++;

		System.err.format("\r%11s %2d questions (%2.2f average / %d) (test %2.2f%%)         ", mName, mQuestionCount, (double) sQuestionCountTotal / sIterations, sIterations, (double) sTestCount * 3 / sIterations * 100);

		return mKnowledge.getCorrectRoom();
    }

    private String getCardFrom(List<String> cards, String correct) {
		// Give a random card to avoid giving away information
		if (mKnowledge.getCorrectCount() == 3) {
			mKnowledge.getStatuses(cards).stream().min(Comparator.comparingInt(c -> {
				if (c.shared) {
					// Try give shared cards first
					return 0;
				} else if (!c.correct && !c.self) {
					// Give a card held by somebody else
					return 1;
				} else if (c.self) {
					// Give own card
					return 2;
				} else {
					// Give correct card
					return 3;
				}
			})).get();
		}

    	// Return an unknown card
    	List<KnowledgeMap.CardStatus> statuses = mKnowledge.getStatuses(cards);
    	statuses.sort(Comparator.comparingInt(KnowledgeMap.CardStatus::getKnownCount));
    	for (KnowledgeMap.CardStatus status : statuses) {
    		if (!status.known) {
    			return status.card;
			}
		}

		// Return one of own cards
		for (String card : mPlayerCards) {
    		if (cards.contains(card)) {
				// Don't help other players by moving them closer to the guess room if we know the answer
    			if (mOtherPlayerSuspects.contains(card)) {
    				continue;
				}
    			return card;
			}
		}

		// Return the correct card
		if (correct != null) {
			return correct;
		}

		// Return a random card
		return cards.get(sRandom.nextInt(cards.size()));
	}

	@Override
    public String getDoor() {
        return getMove();
    }

	@Override
	public void notifyResponse(Log log) {
    	log.iterator();
		while (log.hasNext()) {
			String question = log.next();
			if (!LOG_QUESTION_PATTERN.matcher(question).matches()) continue;
			String answer = log.next();

			boolean change = processLogLine(question, answer);

			if (change) {
				printKnowledge();
			}
		}
	}

	@Override
	public void notifyPlayerName(String playerName) {

	}

	@Override
	public void notifyTurnOver(String playerName, String position) {

	}

	@Override
	public void notifyQuery(String playerName, String query) {
		mQueryPlayer = playerName;
	}

	@Override
	public void notifyReply(String playerName, boolean cardShown) {
		if (cardShown) {
			mQueryPlayer = null;
		}
	}

	private String mQueryPlayer = null;

	@Override
	public String getCard(Cards matchingCards) {
		List<String> matching = new ArrayList<>();
		matchingCards.iterator().forEachRemaining((c) -> matching.add(c.toString()));

		String card = chooseResponseCard(matching);

		// Should not happen, but just in case
		if (card == null) {
			throw new IllegalStateException("Could not choose response card");
		}

		// Add to shown cards list
		showCard(mQueryPlayer, card);

		return card;
	}

	private void showCard(String player, String card) {
		if (!mShownCards.containsKey(player)) {
			mShownCards.put(player, new HashSet<>());
		}
		mShownCards.get(player).add(card);
	}

	private String chooseResponseCard(List<String> matching) {
		// If only one card available show immediately as there is no choice
		if (matching.size() == 1) {
			return matching.get(0);
		}

		// Cards shown to player previously
		Set<String> shownCards = mShownCards.getOrDefault(mQueryPlayer, Collections.emptySet());

		// Separate into card types
		String suspect = matching.stream().filter(SUSPECTS::contains).findAny().orElse(null);
		String weapon = matching.stream().filter(WEAPONS::contains).findAny().orElse(null);
		String room = matching.stream().filter(ROOMS::contains).findAny().orElse(null);

		// Show any already shown card
		if (!Collections.disjoint(shownCards, matching)) {
			// If multiple cards are available, the room won't be chosen at is it last in the list
			return matching.stream().filter(shownCards::contains).findFirst().orElseThrow(NoSuchElementException::new);
		}

		// If own suspect card is available, show to try minimize other players moving us into rooms
		if (mCharacter.equals(suspect)) {
			return suspect;
		}

		// If another player's suspect card is available, don't show to disadvantage them by being continuously moved into rooms
		if (mOtherPlayerSuspects.contains(suspect)) {
			// At least two cards matched so the other will be shown
			suspect = null;
		}

		// Suspect and weapon available
		if (suspect != null && weapon != null && mInitialized) {
			float suspectsShown = shownCards.stream().filter(SUSPECTS::contains).count() / mPlayerSuspectCards.size();
			float weaponsShown = shownCards.stream().filter(WEAPONS::contains).count() / mPlayerWeaponCards.size();

			return suspectsShown < weaponsShown ? suspect : weapon;
		}

		// Show weapons before rooms
		if (weapon != null) {
			return weapon;
		}

		// Show suspects before rooms
		if (suspect != null) {
			return suspect;
		}

		// Worst case, reveal a room
		return room;
	}

    private void parseLog() {
    	mLog.iterator();

    	// Ignore previously parsed lines
    	for (int i = 0; i < mLogCurrentIndex; i++) {
    		mLog.next();
		}

    	while (mLog.hasNext()) {
			String question = mLog.next();
			if (!LOG_QUESTION_PATTERN.matcher(question).matches()) {
				mLogCurrentIndex++;
				continue;
			}
			String answer = mLog.next();

			boolean change = processLogLine(question, answer);

			if (change) {
				printKnowledge();
			}

			mLogCurrentIndex += 2;
		}
	}

	private boolean processLogLine(String question, String answer) {
		Matcher questionMatcher = LOG_QUESTION_PATTERN.matcher(question);
		if (!questionMatcher.matches()) {
			throw new IllegalStateException("Could not parse answer log line: " + question);
		}

		int group = 1;

		String asker = questionMatcher.group(group++);
		String askerCharacter = questionMatcher.group(group++);
		String asked = questionMatcher.group(group++);
		String askedCharacter = questionMatcher.group(group++);

		// Save all player suspect names
		mOtherPlayerSuspects.add(askerCharacter);
		mOtherPlayerSuspects.add(askedCharacter);

		String suspect = questionMatcher.group(group++);
		String weapon = questionMatcher.group(group++);
		String room = questionMatcher.group(group);

		Matcher responseMatcher = LOG_ANSWER_PATTERN.matcher(answer);
		if (!responseMatcher.matches()) {
			throw new IllegalStateException("Could not parse response log line: " + answer);
		}

		group = 1;

		String responder = responseMatcher.group(group++);
		if (!responder.equals(asked)) {
			throw new IllegalStateException("Incorrect responder to question, " + asked + " asked, " + responder + " responded");
		}

		boolean showed = responseMatcher.group(group++).equals(LOG_SHOWED_CARD);
		String response = responseMatcher.group(group);

		// We showed a card
		if (responder.equals(mName)) {
			List<String> matchingCards = Stream.of(suspect, weapon, room)
					.filter(mPlayerCards::contains)
					.collect(Collectors.toList());

			if (matchingCards.size() == 1) {
				showCard(asker, matchingCards.get(0));
			}

			return false;
		}

		boolean change = false;
		if (!showed) {
			change = mKnowledge.setPlayerCardsValue(responder, Arrays.asList(suspect, weapon, room), Value.NotHolding) > 0;
		} else {
			if (asker.equals(mName)) {
				if (response == null) {
					// Dealt with by notifyResponse(Log)
				} else {
					change = mKnowledge.setPlayerCardValue(responder, response, Value.Holding) > 0;
				}
			} else {
				change = mKnowledge.addPlayerMustHaveOne(responder, Arrays.asList(suspect, weapon, room));
			}
		}

		if (change) {
			//System.out.format("%10s asked for %10s with %10s in %10s, %10s %b\n", asker, suspect, weapon, room, responder, showed);
		}

		return change;
	}

	private void printKnowledge() {
		if (getClass() != WolfeTones.class) return;

    	if (getClass() != WolfeTones6.class) return;

		System.out.format("%15s |", "");
		for (String player : mOtherPlayers) {
			System.out.format(" %10s", player);
		}

		System.out.println();

		for (String card : CARDS) {
			KnowledgeMap.CardStatus cardStatus = mKnowledge.getStatus(card);
			String c = "";
			if (cardStatus.self) {
				c = "M";
			} else if (cardStatus.shared) {
				c = "S";
			} else if (cardStatus.correct) {
				c = "C";
			}
			System.out.format("%15s %1s |", cardStatus.correct ? card.toUpperCase() : card, c);
			for (String player : mOtherPlayers) {
				KnowledgeMap.CardPlayerStatus status = mKnowledge.getPlayerCardStatus(player, card);

				String s = " ";
				switch (status.getValue()) {
					case Holding:
						s = "O";
						break;
					case NotHolding:
						s = "X";
						break;
					case SuspectedHolding:
						s = "?";
						break;
					case SuspectedNotHolding:
						s = "!";
						break;
				}

				System.out.format(" %10s", s);
			}
			System.out.println();
		}
	}

	private class KnowledgeMap {
		private Map<String, CardStatus> mCardStatusMap = new LinkedHashMap<>();
		private Map<String, Map<String, CardPlayerStatus>> mCardPlayerMap = new LinkedHashMap<>();
		private Map<String, Map<String, CardPlayerStatus>> mPlayerCardMap = new LinkedHashMap<>();

		private String mCorrectSuspect;
		private String mCorrectWeapon;
		private String mCorrectRoom;

		private Set<MustHaveOne> mMustHaveOnes = new HashSet<>();

		private int mCorrectCount;

		private boolean mUpdatingMustHaveOnes = false;

		private KnowledgeMap() {
			// Initialize player maps in card player map
			for (String c : CARDS) {
				mCardPlayerMap.put(c, new HashMap<>());

				CardStatus status = new CardStatus();

				status.card = c;
				if (mPlayerCards.contains(c)) {
					status.known = true;
					status.self = true;
					status.knownCount = mOtherPlayers.size();
				} else if (mDeck.isSharedCard(c)) {
					status.known = true;
					status.shared = true;
					status.knownCount = mOtherPlayers.size();
				}

				mCardStatusMap.put(c, status);
			}

			// Initialize card maps in player card map
			for (String p : mOtherPlayers) {
				mPlayerCardMap.put(p, new HashMap<>());
			}

			// Initialize statuses
			for (String c : CARDS) {
				for (String p : mOtherPlayers) {
					CardPlayerStatus status = new CardPlayerStatus();

					if (mPlayerCards.contains(c)) {
						status.value = Value.NotHolding;
					} else if (mDeck.isSharedCard(c)) {
						status.value = Value.NotHolding;
					}

					mCardPlayerMap.get(c).put(p, status);
					mPlayerCardMap.get(p).put(c, status);
				}
			}
		}

		private CardStatus getStatus(String card) {
			return mCardStatusMap.get(card);
		}

		private List<CardStatus> getStatuses(List<String> cards) {
			List<CardStatus> cardStatuses = new ArrayList<>(mCardStatusMap.values());

			cardStatuses.removeIf(s -> !cards.contains(s.card));

			return cardStatuses;
		}

		private Map<String, CardPlayerStatus> getPlayerMap(String card) {
			return new LinkedHashMap<>(mCardPlayerMap.get(card));
		}

		private List<CardPlayerStatus> getPlayerList(String card) {
			return new ArrayList<>(getPlayerMap(card).values());
		}

		private Map<String, CardPlayerStatus> getCardMap(String player) {
			return new LinkedHashMap<>(mPlayerCardMap.get(player));
		}

		private List<CardPlayerStatus> getCardList(String player) {
			return new ArrayList<>(getCardMap(player).values());
		}

		CardPlayerStatus getPlayerCardStatus(String player, String card) {
			return mPlayerCardMap.get(player).get(card);
		}

		private int getCorrectCount() {
			return mCorrectCount;
		}

		private String getCorrectSuspect() {
			return mCorrectSuspect;
		}

		private String getCorrectWeapon() {
			return mCorrectWeapon;
		}

		private String getCorrectRoom() {
			return mCorrectRoom;
		}

		private int setPlayerCardValue(String player, String card, Value value) {
			CardPlayerStatus cardPlayerStatus = getPlayerCardStatus(player, card);
			int changes = 0;
			if (cardPlayerStatus.value != value) {
				cardPlayerStatus.value = value;

				changes = 1;

				CardStatus cardStatus = mCardStatusMap.get(card);

				if (value == Value.Holding) {
					// Update card status
					cardStatus.known = true;
					cardStatus.knownCount++;

					// Only one player can have a card
					List<String> otherPlayers = new ArrayList<>(mOtherPlayers);
					otherPlayers.remove(player);
					changes += setPlayersCardValue(otherPlayers, card, Value.NotHolding);

					// Player has a certain number of cards
					List<CardPlayerStatus> playerCardStatuses = getCardList(player);
					int playerHeldCount = (int) playerCardStatuses.stream()
							.map(CardPlayerStatus::getValue)
							.filter(Value.Holding::equals)
							.count();
					if (playerHeldCount == mPlayerNumCards) {
						List<String> otherCards = new ArrayList<>(mPlayerCards);
						otherCards.removeIf(c -> getPlayerCardStatus(player, c).value == Value.Holding);
						changes += setPlayerCardsValue(player, otherCards, Value.NotHolding);
					}

					// All cards of a type found
					for (List<String> cardList : CARD_LISTS) {
						String remainingCard = null;
						for (String c : cardList) {
							CardStatus cs = mCardStatusMap.get(c);

							if (cs.correct) {
								remainingCard = null;
								break;
							}

							if (!cs.known) {
								if (remainingCard != null) {
									remainingCard = null;
									break;
								} else {
									remainingCard = c;
								}
							}
						}

						if (remainingCard != null) {
							changes += setPlayersCardValue(mOtherPlayers, remainingCard, Value.NotHolding);
						}
					}
				} else if (value == Value.NotHolding) {
					// Additional card known
					cardStatus.knownCount++;

					// No players holding card
					List<CardPlayerStatus> cardPlayerStatuses = getPlayerList(card);
					int notHeldCount = (int) cardPlayerStatuses.stream()
							.map(CardPlayerStatus::getValue)
							.filter(Value.NotHolding::equals)
							.count();
					if (notHeldCount == mOtherPlayers.size()) {
						cardStatus.known = true;
						cardStatus.correct = true;

						if (SUSPECTS.contains(card)) {
							mCorrectSuspect = card;
						} else if (WEAPONS.contains(card)) {
							mCorrectWeapon = card;
						} else if (ROOMS.contains(card)) {
							mCorrectRoom = card;
						} else {
							throw new IllegalStateException("Unknown correct card group");
						}

						mCorrectCount++;
					}

					// Player has a certain number of cards
					List<CardPlayerStatus> playerCardStatuses = getCardList(player);
					int playerNotHeldCount = (int) playerCardStatuses.stream()
							.map(CardPlayerStatus::getValue)
							.filter(Value.NotHolding::equals)
							.count();
					if (playerNotHeldCount == mPlayerCards.size() - mPlayerNumCards) {
						List<String> otherCards = new ArrayList<>(mPlayerCards);
						otherCards.removeIf(c -> getPlayerCardStatus(player, c).value == Value.NotHolding);
						changes += setPlayerCardsValue(player, otherCards, Value.Holding);
					}
				}

				updateMustHaveOnes();
			}
			return changes;
		}

		private int setPlayersCardValue(List<String> players, String card, Value value) {
			int changes = 0;

			for (String p : players) {
				changes += setPlayerCardValue(p, card, value);
			}

			return changes;
		}

		private int setPlayerCardsValue(String player, List<String> cards, Value value) {
			int changes = 0;

			for (String c : cards) {
				changes += setPlayerCardValue(player, c, value);
			}

			return changes;
		}

		private boolean addPlayerMustHaveOne(String player, List<String> cards) {
			MustHaveOne mustHaveOne = new MustHaveOne();
			mustHaveOne.player = player;
			mustHaveOne.cards = new ArrayList<>(cards);

			boolean modified = false;

			for (String c : cards) {
				CardPlayerStatus status = getPlayerCardStatus(player, c);
				if (status.value != Value.Holding && status.value != Value.NotHolding) {
					status.value = Value.SuspectedHolding;
					modified = true;
				}
			}

			mMustHaveOnes.add(mustHaveOne);

			updateMustHaveOnes();

			return modified;
		}

		private void updateMustHaveOnes() {
			if (mUpdatingMustHaveOnes) return;

			mUpdatingMustHaveOnes = true;
			int changes;
			do {
				changes = 0;
				for (MustHaveOne mustHaveOne : mMustHaveOnes) {
					List<String> heldCards = new ArrayList<>();
					List<String> unknownCards = new ArrayList<>();
					for (String c : mustHaveOne.cards) {
						CardPlayerStatus status = getPlayerCardStatus(mustHaveOne.player, c);
						if (status.value == Value.Holding) {
							heldCards.add(c);
						} else if (status.value != Value.NotHolding) {
							unknownCards.add(c);
						}
					}

					if (!heldCards.isEmpty()) {
						// A card is held, set other cards to suspected not holding
						changes += setPlayerCardsValue(mustHaveOne.player, unknownCards, Value.SuspectedNotHolding);
						mustHaveOne.player = null;
					} else if (unknownCards.size() == 1) {
						// A single card is unknown, must be held
						changes += setPlayerCardValue(mustHaveOne.player, unknownCards.get(0), Value.Holding);
						mustHaveOne.player = null;
					}
				}

				mMustHaveOnes.removeIf(mho -> mho.player == null);
			} while (changes > 0);
			mUpdatingMustHaveOnes = false;
		}

		private class CardStatus {
			private String card;
			private boolean known;

			private int knownCount;

			private boolean correct;
			private boolean self;
			private boolean shared;

			private String getCard() {
				return card;
			}

			private boolean isUnknown() {
				return !known;
			}

			private int getKnownCount() {
				return knownCount;
			}
		}

		private class CardPlayerStatus {
			private Value value = Value.Unknown;

			private Value getValue() {
				return value;
			}
		}

		private class MustHaveOne {
			private String player;
			private List<String> cards;
		}
	}

	private class PathFinder {
		/**
		 * Returns the distance between two tiles along axes at right angles.
		 *
		 * @param a tile A
		 * @param b tile B
		 * @return the Manhattan distance between the two tiles
		 */
		private int tileManhattanDistance(Tile a, Tile b) {
			return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
		}

		/**
		 * Finds the shortest path available between two locations, taking into account room entrance corridors
		 *
		 * @param start from location.
		 * @param target to location.
		 * @return a list of tiles containing the shortest path from {@code start} to {@code target}.
		 */
		private List<Tile> findShortestPathAdvanced(Object start, Object target) {
			List<Tile> path;
			if (start instanceof Room && target instanceof Room) { // Two
				Room startRoom = (Room) start;
				Room targetRoom = (Room) target;

				// Loop through each possible combination of entrance corridors
				// to check whether the player can move between the two rooms
				List<Tile> min = null;
				for (int i = 0; i < startRoom.getNumberOfDoors(); i++) {
					Tile fromRoomDoor = new Tile(startRoom.getDoorCoordinates(i));
					for (int j = 0; j < targetRoom.getNumberOfDoors(); j++) {
						Tile toRoomDoor = new Tile(targetRoom.getDoorCoordinates(j));
						if ((path = findShortestPath(fromRoomDoor, toRoomDoor)) != null) {
							if (min == null || path.size() < min.size()) {
								min = path;
							}
						}
					}
				}
				return min;
			} else if (!(start instanceof Room) && !(target instanceof Room)) { // No rooms
				return findShortestPath((Tile) start, (Tile) target);
			} else { // One room
				// Find which of from/to locations is the room
				Room loopRoom = start instanceof Room ? (Room) start : (Room) target;

				// Loop through each entrance corridor
				List<Tile> min = null;
				for (int i = 0; i < loopRoom.getNumberOfDoors(); i++) {
					Tile fromTile = loopRoom == start ? new Tile(loopRoom.getDoorCoordinates(i)) : (Tile) start;
					Tile toTile = loopRoom == target ? new Tile(loopRoom.getDoorCoordinates(i)) : (Tile) target;
					if ((path = findShortestPath(fromTile, toTile)) != null) {
						if (min == null || path.size() < min.size()) {
							min = path;
						}
					}
				}
				return min;
			}
		}

		/**
		 * Finds the shortest path available between two tiles.
		 *
		 * @param start The starting tile.
		 * @param target The target tile.
		 * @return A list of tiles containing the shortest path from {@code start} to {@code target}.
		 */
		private List<Tile> findShortestPath(Tile start, Tile target) {
			int maxMoves = Integer.MAX_VALUE;

			// If moving to same position must move 1 tile and back
			if (start.equals(target)) {
				for (String direction : DIRECTIONS) {
					if (start.isValidMove(direction)) {
						return Arrays.asList(start, start.getNewPosition(direction), start);
					}
				}
			}

			// Map of tile to their shortest paths
			Map<Tile, List<Tile>> paths = new HashMap<>();

			// Priority queue to hold further explorable tiles
			PriorityQueue<Tile> queue = new PriorityQueue<>(Comparator.comparingInt(tile -> expectedMovesForNode(tile, target, paths.get(tile))));

			// Add the first
			paths.put(start, new ArrayList<>());
			queue.add(start);

			while (!queue.isEmpty()) {
				Tile currentTile = queue.poll();
				if (currentTile == null) continue;

				// If path has already exceeded max moves, don't check neighbours
				if (expectedMovesForNode(currentTile, target, paths.get(currentTile)) > maxMoves) continue;

				// Loop through neighbours
				for (String direction : DIRECTIONS) {
					if (!currentTile.isValidMove(direction)) continue;

					Tile neighbouringTile = currentTile.getNewPosition(direction);

					// Don't loop through existing tiles
					if (paths.get(currentTile).contains(neighbouringTile)) continue;

					// Append current tile to the path
					List<Tile> path = new ArrayList<>(paths.get(currentTile));
					path.add(currentTile);

					if (paths.containsKey(neighbouringTile)) {
						// Existing node
						List<Tile> neighbouringTilePath = paths.get(neighbouringTile);

						// If tile already has node and path is shorter ignore long route
						if (neighbouringTilePath.size() <= path.size()) {
							continue;
						}
					}

					paths.put(neighbouringTile, path);

					// Attempting to find target
					if (!neighbouringTile.equals(target)) {
						if (!queue.contains(neighbouringTile)) {
							queue.add(neighbouringTile);
						}
					} else {
						// Max moves is now the shortest path length
						maxMoves = path.size();

						// If target has been found no need to check neighbours further
						break;
					}
				}
			}

			// If path to target was found return
			if (paths.containsKey(target)) {
				List<Tile> path = paths.get(target);
				path.add(target);
				return path;
			}

			return null;
		}

		/**
		 * Calculates the expected total moves required to reach the target tile from a node.
		 *
		 * Returns the sum of the current path to the tile along and
		 * the Manhattan distance to the target tile, assuming the
		 * optimal route with Manhattan distance is available.
		 *
		 * @param start The start tile
		 * @param target The target tile
		 * @return The expected total moves required to reach the {@code target} from the {@code node}
		 */
		private int expectedMovesForNode(Tile start, Tile target, List<Tile> path) {
			return path.size() + tileManhattanDistance(start, target);
		}

		private String tilePathToMovementString(List<Tile> tiles) {
			StringBuilder movement = new StringBuilder();

			Tile previous = null;
			for (Tile tile : tiles) {
				if (previous == null) {
					previous = tile;
					continue;
				}

				// If door use door index as move
				if (mMap.isDoor(tile.coordinates, previous.coordinates)) {
					Room room = mMap.getRoom(previous.coordinates);

					if (room.getNumberOfDoors() > 1) {
						// Find out which door in room
						int doorIndex = -1;
						for (int i = 0; i < room.getNumberOfDoors(); i++) {
							Coordinates door = room.getDoorCoordinates(i);
							if (door.getCol() == previous.x && door.getRow() == previous.y) {
								doorIndex = i;
								break;
							}
						}

						if (doorIndex < 0) {
							throw new IllegalStateException("Could not discover room door");
						} else {
							movement.append(doorIndex + 1);
						}
					}
				}

				movement.append(previous.directionToTile(tile));

				previous = tile;
			}

			return movement.toString();
		}
	}

	public enum Value {
		Unknown, Holding, NotHolding, SuspectedHolding, SuspectedNotHolding
	}

	private class Tile {
    	private final int x;
    	private final int y;
    	private final Coordinates coordinates;

    	private Tile(Coordinates coordinates) {
    		this.x = coordinates.getCol();
    		this.y = coordinates.getRow();
    		this.coordinates = coordinates;
		}

		private boolean isValidMove(String direction) {
    		return mMap.isValidMove(coordinates, direction);
		}

		private Tile getNewPosition(String direction) {
    		if (!isValidMove(direction)) {
    			throw new IllegalArgumentException("Can't move in direction " + direction);
			}

			return new Tile(mMap.getNewPosition(coordinates, direction));
		}

		private String directionToTile(Tile tile) {
    		if (tile.x == this.x + 1 && tile.y == this.y) {
    			return DIRECTION_RIGHT;
			} else if (tile.x == this.x - 1 && tile.y == this.y) {
    			return DIRECTION_LEFT;
			} else if (tile.x == this.x && tile.y == this.y + 1) {
    			return DIRECTION_DOWN;
			} else if (tile.x == this.x && tile.y == this.y - 1) {
				return DIRECTION_UP;
			} else {
    			throw new IllegalArgumentException("Tile provided is not an adjacent tile ([" + x + ", " + y + "] to [" + tile.x + ", " + tile.y + "])");
			}
		}

		@Override
		public String toString() {
    		return "Tile[" + x + ", " + y + "]";
		}

		@Override
		public boolean equals(Object obj) {
    		if (!(obj instanceof Tile)) {
    			return false;
			}

			Tile t = (Tile) obj;
    		return t.x == this.x && t.y == this.y;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + x;
			hash = 31 * hash + y;
			return hash;
		}
	}
}
