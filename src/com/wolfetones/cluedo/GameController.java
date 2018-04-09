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

package com.wolfetones.cluedo;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.*;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Game;
import com.wolfetones.cluedo.board.Location;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.PlayerList;
import com.wolfetones.cluedo.game.Suggestion;
import com.wolfetones.cluedo.ui.*;
import com.wolfetones.cluedo.ui.component.*;
import com.wolfetones.cluedo.ui.panel.*;
import com.wolfetones.cluedo.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {
    private static final boolean DEMO_MODE = Boolean.parseBoolean(System.getProperty("debug"));
    private static final boolean ALLOW_SKIPPING_ANIMATIONS = Boolean.parseBoolean(System.getProperty("debug"));

    private static Random sRandom = new Random();

    /**
     * Setting to enable cheat command that gives the solution.
     */
    private static final boolean CHEAT_ENABLED = true;

    /**
     * Layers for the board's {@code JLayeredPane}.
     */
    private static final Integer BOARD_LAYER_START_TILE_CIRCLES = 0;
    private static final Integer BOARD_LAYER_TILES = 1;
    private static final Integer BOARD_LAYER_ROOM_NAMES = 2;
    private static final Integer BOARD_LAYER_TOKENS = 3;
    private static final Integer BOARD_LAYER_DICE = 4;
    private static final Integer BOARD_LAYER_PLAYERS = 5;
    private static final Integer BOARD_LAYER_ACTIONS = 6;
    private static final Integer BOARD_LAYER_CARDS = 7;
    private static final Integer BOARD_LAYER_CURSOR = 8;
    private static final Integer BOARD_LAYER_PANELS = 9;

    /**
     * Prefix placed in front of commands to hide from the list of valid commands.
     *
     * Used with commands such as quit and cheat which are always available
     * but not necessary to be shown every time.
     */
    private static final String HIDDEN_COMMAND_PREFIX = "@";

    private static final Map<String, String> HELP_COMMANDS = new HashMap<>() {
        private void add(String command, String args, String description) {
            String title = "| " + command;
            if (args != null) {
                title += " " + args;
            }
            title += " |";

            String box = new String(new char[title.length()]).replace("\0", "-");

            String text = box + "\n" + title + "\n" + box + "\n" + description + "\n";

            put(command, text);
        }

        {
            add(COMMAND_ROLL, null, "Roll dice and initiate movement. Once dice have been rolled, at least one move must be completed, and secret passages can no longer be used in the same turn.");
            add(COMMAND_PASSAGE, null, "Use secret passage. Moves the player to the room on the opposite corner of the board. Once a secret passage has been used, no other movement is allowed.");
            add(COMMAND_QUESTION, "(#suspect #weapon)", "Make a suggestion as to who may have committed the murder in the current room. Once a question has been posed, other players are asked in clockwise order whether they have any of the cards involved in this suggestion. If they have any of the cards, they must choose to show one of them to the player who made the suggestion.");
            add(COMMAND_ACCUSE, "(#suspect #weapon #room)", "Make a final accusation as to who committed the murder. If the final accusation is correct, the player wins, otherwise they are eliminated.");
            add(COMMAND_NOTES, null, "Show the notes panel. Used to keep track of knowledge of what players have what cards to assist in the investigation of who committed the murder.");
            add(COMMAND_LOG, null, "Show the suggestion history panel. Lists all suggestions and incorrect final accusations that have been made by players in the past.");
            add(COMMAND_DONE, null, "Finish turn. Play is passed to the next active player.");
            add(COMMAND_QUIT, null, "Quit game. Shows the correct solution to the game and exits.");
            if (CHEAT_ENABLED) add(COMMAND_CHEAT, null, "Shows the correct solution to the game.");
        }
    };

    /**
     * Commands that can be executed by the user
     */
    public static final String COMMAND_ROLL = "roll";
    public static final String COMMAND_PASSAGE = "passage";
    public static final String COMMAND_DONE = "done";
    public static final String COMMAND_QUIT = "quit";
    public static final String COMMAND_QUESTION = "question";
    public static final String COMMAND_ACCUSE = "accuse";
    public static final String COMMAND_NOTES = "notes";
    public static final String COMMAND_LOG = "log";
    public static final String COMMAND_CHEAT = "cheat";
    public static final String COMMAND_HELP = "help";

    public static final String COMMAND_LEFT = "l";
    public static final String COMMAND_UP = "u";
    public static final String COMMAND_RIGHT = "r";
    public static final String COMMAND_DOWN = "d";
    public static final String COMMAND_STOP = "stop";

    private static final String COMMAND_SHOW = "show";

    private static final Map<Integer, String> KEY_COMMAND_MAP = new HashMap<>() {{
        // Commands
        put(KeyEvent.VK_R, COMMAND_ROLL);
        put(KeyEvent.VK_P, COMMAND_PASSAGE);
        put(KeyEvent.VK_Q, COMMAND_QUESTION);
        put(KeyEvent.VK_A, COMMAND_ACCUSE);
        put(KeyEvent.VK_N, COMMAND_NOTES);
        put(KeyEvent.VK_L, COMMAND_LOG);
        put(KeyEvent.VK_S, COMMAND_SHOW);
        put(KeyEvent.VK_D, COMMAND_DONE);

        // Directions
        put(KeyEvent.VK_LEFT, COMMAND_LEFT);
        put(KeyEvent.VK_UP, COMMAND_UP);
        put(KeyEvent.VK_RIGHT, COMMAND_RIGHT);
        put(KeyEvent.VK_DOWN, COMMAND_DOWN);
        put(KeyEvent.VK_ESCAPE, COMMAND_STOP);
    }};

    /**
     * Game instance
     */
    private Game mGame = new Game();

    private PlayerList mPlayers = new PlayerList();

    /**
     * Swing containers
     */
    private JFrame mMainFrame;

    private JLayeredPane mBoardLayeredPane;
    private JPanel mBoardTilePanel;
    private DicePanel mBoardDicePanel;
    private JPanel mBoardCursorPanel;
    private int mTileSize;

    private OutputPanel mOutputPanel;
    private InputPanel mInputPanel;

    private PlayersPanel mPlayersPanel;
    private ActionPanel mActionPanel;
    private CardAnimationsPanel mCardAnimationsPanel;
    private NotesPanel mNotesPanel;
    private LogPanel mHistoryPanel;

    private SlideOutCardsPanel mPlayerCardsPanel;
    private SlideOutCardsPanel mUndistributedCardsPanel;

    private SlideOutPanel mHistorySlideOutPanel;
    private SlideOutPanel mNotesSlideOutPanel;

    /**
     * Click action
     */
    private MouseListener mClickAction;

    /**
     * Path Finding
     */
    private boolean mPathFindingEnabled = false;
    private List<TokenOccupiableTile> mPreviousPath;
    private MouseListener mTilePathFindingListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            onTileHover((TileComponent) e.getComponent());
        }
    };

    /**
     * Scanner for reading stdin
     */
    private Scanner mInputScanner;

    public static void main(String[] args) {
        System.out.println("Welcome to " + Config.TITLE + " by");
        System.out.println(Config.AUTHOR);

        // Disable HiDPI scaling
        System.setProperty("sun.java2d.uiScale.enabled", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
            // Ignore
        }

        new GameController();
    }

    private GameController() {
        setupFrame();
        setupPlayers();
        setupBoardWithPlayers();

        // Dice throw animation
        throwDiceForOrder();

        mPlayers.forEach(mGame::addPlayer);
        mGame.start();

        // Card dealing animation
        dealCards();

        // Input scanner for parsing stdin
        mInputScanner = new Scanner(System.in);

        // Keep performing new turns until the game is over
        while (true) {
            if (!performTurn()) {
                break;
            }
        }

        // Once game is over prevent user from interacting
        mActionPanel.setVisible(false);
        mInputPanel.setEnabled(false);
    }

    /**
     * Requests that the user enter a valid command from a list of possible commands
     *
     * Allows input as array instead of list
     *
     * @param question Prompt to provide to the user when listing valid commands
     * @param validCommands Valid commands
     * @return The command that the user entered
     */
    private String[] readCommand(String question, String... validCommands) {
        return readCommand(question, Arrays.asList(validCommands));
    }

    /**
     * Requests that the user enter a valid command from a list of possible commands
     *
     * @param question Prompt to provide to the user when listing valid commands
     * @param validCommandsList Valid commands
     * @return The command that the user entered
     */
    private String[] readCommand(String question, List<String> validCommandsList) {
        // Maintain a separate list for commands that are printed, excluding hidden commands
        List<String> printedCommandsList = new ArrayList<>();

        // Make copy of commands list
        validCommandsList = new ArrayList<>(validCommandsList);

        // Iterate valid commands to find hidden commands
        ListIterator<String> commandIterator = validCommandsList.listIterator();
        while (commandIterator.hasNext()) {
            String current = commandIterator.next();

            // Only add the command to the printed commands list if it is not hidden
            if (current.startsWith(HIDDEN_COMMAND_PREFIX)) {
                // Don't include the hidden command prefix in the actual command
                commandIterator.set(current.substring(HIDDEN_COMMAND_PREFIX.length()));
            } else {
                printedCommandsList.add(current);
            }
        }

        // Print the question along with the valid commands/answers
        System.out.println(question + " [valid: " + Util.implode(printedCommandsList, ", ") + "]");

        mInputPanel.setCommandHints(printedCommandsList);

        final List<String> finalValidCommandsList = validCommandsList;
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String command = KEY_COMMAND_MAP.get(e.getKeyCode());
                if (finalValidCommandsList.contains(command)) {
                    mInputPanel.append(command);
                }
            }
        };

        mMainFrame.addKeyListener(keyListener);

        String[] command;
        String line;
        while (true) {
            line = mInputScanner.nextLine();

            // Interrupt/end of text
            if (line.equals("\3")) {
                command = new String[] {null};
                break;
            }

            // Trim, remove duplicate spaces, and use lower case
            line = line.trim().replaceAll(" +", " ").toLowerCase();

            // Wait for text
            if (line.length() == 0) {
                continue;
            }

            // Split line into command and arguments
            command = line.split(" ");

            // Exit the loop if the command is valid
            if (validCommandsList.contains(command[0])) {
                break;
            }

            System.out.println("Invalid command '" + command[0] + "'");
        }

        mMainFrame.removeKeyListener(keyListener);

        mInputPanel.setCommandHints(null);
        mInputPanel.clear();

        return command;
    }

    /**
     * Performs all necessary steps for a player turn
     */
    private boolean performTurn() {
        Player player = mGame.nextTurn();

        mPlayersPanel.setTopPlayer(player);
        mPlayersPanel.setActivePlayer(0);

        mOutputPanel.clear();

        passToPlayer(player, null);

        // Update player cards panel
        mPlayerCardsPanel.setCards(player.getCards());

        // Update undistributed cards panel
        mUndistributedCardsPanel.setCards(mGame.getUndistributedCards());

        // Update notes panel
        mNotesPanel.setCurrentPlayer(player);
        mNotesSlideOutPanel.setVisible(true);
        mNotesSlideOutPanel.reposition();

        // Update history panel
        mHistoryPanel.setCurrentPlayer(player);
        mHistorySlideOutPanel.setVisible(!mGame.getLog().isEmpty());
        mHistorySlideOutPanel.reposition();

        // If game is finished, everyone else was eliminated and this user wins by default
        if (mGame.isFinished()) {
            System.out.println("All other players have been eliminated, you win by default!");

            mCardAnimationsPanel.finalAccusation(mGame.getSolution(), mGame.getSolution());

            return false;
        }

        System.out.println(player.getName() + "'s move (" + player.getCharacter().getName() + ")");

        List<String> bubbleLines = new ArrayList<>();
        if (mGame.canPoseQuestion()) {
            // Moved since last turn
            bubbleLines.add("Looks like somebody has moved me here, I can question right away!");
            bubbleLines.add("I don't remember being here at the end of my last turn... anyway, let's as some questions!");
            bubbleLines.add("Somebody accusing me of murder... unbelievable! They'll see when I uncover the murderer!");
            bubbleLines.add("Ugh, just as I was about to uncover the murderer they've thrown me off the trail...");
        } else if (mGame.canUsePassage()) {
            Room room = mGame.getCurrentPlayerLocation().asRoom();
            String passageRoomName = room.getPassageRoom().getName().toLowerCase();

            bubbleLines.add("Hmm, I could use the secret passage to the " + passageRoomName + " from here..");
            bubbleLines.add("I heard there's a secret passage around here, I wonder where it leads to?");
            bubbleLines.add("That wall looks weird.. is that.. a secret door?!");
            bubbleLines.add("The old passage to the " + passageRoomName + ".. I bet the murderer used it to escape!");

            if (passageRoomName.equals("kitchen")) {
                bubbleLines.add("A secret passage to the kitchen?! Well, I am quite hungry...");
                bubbleLines.add("All this questioning is making me hungry, let's go to the kitchen through this passage");
            }

            // Room has secret passage
            mPlayersPanel.showBubble(player, "I could use the secret passage to the " + room.getPassageRoom().getName().toLowerCase() + " here?!");
        } else if (mGame.getCurrentPlayerLocation().isRoom()) {
            bubbleLines.add("Let's try get to a different room for some more questions.");
            bubbleLines.add("That's enough investigation in this room, time to check out another room.");
            bubbleLines.add("I've inspected every corner of the room, on to the next one!");
            bubbleLines.add("I've gathered some useful evidence in here, let's see if I can get some more elsewhere!");
        } else if (mGame.getCurrentPlayerLocation().asTile() instanceof StartTile) {
            bubbleLines.add("Let's get started with this investigation. Roll the dice!");
            bubbleLines.add("I better start investigating, let's roll the dice and get to a room");
            bubbleLines.add("This mystery is about to be solved! Let's roll the dice and get to a room");
            bubbleLines.add("Hoping for a good dice roll so I can get to a room");
            bubbleLines.add("I should get to a room to start asking some questions, let's roll the dice!");
        } else {
            bubbleLines.add("I should get to a room to start asking some questions, let's roll the dice!");
            bubbleLines.add("These corridors creep me out.. let's try get to a room and ask some questions!");
            bubbleLines.add("Guess all I can do is roll the dice and see where it gets me..");
            bubbleLines.add("I better get a double 6 this time!");
            bubbleLines.add("I wouldn't want to get caught alone with the murderer in the corridors..");
        }

        mPlayersPanel.showBubble(player, bubbleLines.get(sRandom.nextInt(bubbleLines.size())));

        List<String> commands = new ArrayList<>();
        while (true) {
            // Remove previous commands
            commands.clear();

            // Add commands depending on whether they are available
            if (mGame.canRollDice()) commands.add(COMMAND_ROLL);
            if (mGame.canUsePassage()) commands.add(COMMAND_PASSAGE);
            if (mGame.canPoseQuestion()) commands.add(COMMAND_QUESTION);
            if (mGame.canMakeFinalAccusation()) commands.add(COMMAND_ACCUSE);
            if (mGame.isTurnFinished()) commands.add(COMMAND_DONE);

            // Cheat, quit, notes, log and help are always available but hidden
            if (CHEAT_ENABLED) commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_CHEAT);
            commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_NOTES);
            commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_LOG);
            commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_QUIT);
            commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_HELP);

            mActionPanel.updateStatus(mGame);

            // Read command
            String[] args = readCommand("Choose action", commands);
            String command = args[0];

            // Hide all actions except done
            mActionPanel.hideAllExceptDone();

            if (command == null) {
                // Ignore and continue
                System.err.println("Unexpected interrupt");
            } else if (command.equals(COMMAND_DONE)) {
                break;
            } else if (command.equals(COMMAND_QUIT)) {
                performQuit();
            } else if (command.equals(COMMAND_CHEAT)) {
                performCheat();
            } else if (command.equals(COMMAND_HELP)) {
                performHelp(args, commands);
            } else if (command.equals(COMMAND_ROLL)) {
                performRoll(player);
            } else if (command.equals(COMMAND_PASSAGE)) {
                performPassage();
            } else if (command.equals(COMMAND_QUESTION)) {
                performQuestion(player, args);
            } else if (command.equals(COMMAND_ACCUSE)) {
                if (performAccuse(player, args)) {
                    // Don't continue if accusation was correct
                    return false;
                }
            } else if (command.equals(COMMAND_NOTES)) {
                performNotes();
            } else if (command.equals(COMMAND_LOG)) {
                performLog();
            }
        }

        // Hide bubbles
        mPlayersPanel.hideBubbles();

        // Hide all panels
        mPlayerCardsPanel.slideOut();
        mUndistributedCardsPanel.slideOut();
        mNotesSlideOutPanel.slideOut();
        mHistorySlideOutPanel.slideOut();

        return true;
    }

    private void performQuit() {
        System.out.println("The solution was: " + mGame.getSolution().asHumanReadableString());
        System.exit(0);
    }

    private void performCheat() {
        System.out.println("The solution is: " + mGame.getSolution().asHumanReadableString());
    }

    private void performHelp(String[] args, List<String> commands) {
        System.out.println("Usage: help ([command|list])");
        if (args.length > 1) {
            switch (args[1]) {
                case "all":
                    HELP_COMMANDS.values().forEach(System.out::println);
                    break;
                case "list":
                    HELP_COMMANDS.keySet().forEach(System.out::println);
                    break;
                default:
                    if (HELP_COMMANDS.containsKey(args[1])) {
                        System.out.println(HELP_COMMANDS.get(args[1]));
                    } else {
                        System.out.println("Invalid command " + args[1] + ". To view list of all commands use `help list`");
                    }
                    break;
            }
        } else {
            System.out.println("Brief help for the currently valid commands is listed below. To view help for a specific command use `help [command]`, or to view a list of commands use `help list`.");

            commands.stream().filter((c) -> !c.startsWith(HIDDEN_COMMAND_PREFIX))
                    .forEach((c) -> System.out.println(HELP_COMMANDS.get(c)));
        }
    }

    private void performPassage() {
        mGame.usePassage();
    }

    private void performRoll(Player player) {
        int[] dice = new int[Game.NUM_DICE];

        // Show cursor panel to allow force finishing dice roll
        setClickAction(mBoardDicePanel::forceFinish, mBoardTilePanel, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Roll dice
        mBoardDicePanel.rollDice(dice, false);

        // Hide cursor panel
        setClickAction(null, null, null);

        // Game will read dice values from dice array if they are not 0
        int remainingMoves = mGame.rollDice(dice);

        mPlayersPanel.showBubble(player, "I rolled " + remainingMoves);
        System.out.println("Rolled " + Util.implode(Arrays.stream(dice).boxed().collect(Collectors.toList()), "+") + " = " + remainingMoves);

        System.out.println("Click on board tiles to move, or alternatively:");

        setPathFindingEnabled(true);

        // Exit if in room
        Location startLocation = mGame.getCurrentPlayerLocation();
        if (startLocation.isRoom()) {
            List<RoomTile> corridorTiles = startLocation.asRoom().getEntranceCorridors();
            corridorTiles.sort(Comparator.comparingInt(Tile::getX));
            String[] validCommands = new String[corridorTiles.size()];
            for (int i = 0; i < corridorTiles.size(); i++) {
                corridorTiles.get(i).getButton().setDoorHint(i + 1);
                validCommands[i] = Integer.toString(i + 1);
            }
            String entranceCommand = readCommand("Choose room exit (or use board tiles)", validCommands)[0];
            // If not interrupted by path finding/UI
            if (entranceCommand != null) {
                int entranceCorridor = Integer.parseInt(entranceCommand) - 1;

                mGame.moveTo(corridorTiles.get(entranceCorridor).getDoorTile());
            }

            corridorTiles.forEach(t -> t.getButton().setDoorHint(0));
        }

        // Make sure we haven't moved directly to another room using path finding/UI
        if (mGame.getTurnRemainingMoves() != 0) {
            Tile moveTile = null;
            CorridorTile currentTile = mGame.getCurrentPlayerLocation().asTile();
            List<String> validCommands = new ArrayList<>(5);
            while ((remainingMoves = mGame.getTurnRemainingMoves()) > 0) {
                validCommands.clear();
                if (currentTile == null) {
                    throw new IllegalStateException("Current tile cannot be null");
                }
                if (currentTile.canMoveLeft()) validCommands.add(COMMAND_LEFT);
                if (currentTile.canMoveUp()) validCommands.add(COMMAND_UP);
                if (currentTile.canMoveRight()) validCommands.add(COMMAND_RIGHT);
                if (currentTile.canMoveDown()) validCommands.add(COMMAND_DOWN);
                if (mGame.canStopMoving()) validCommands.add(HIDDEN_COMMAND_PREFIX + COMMAND_STOP);

                // Update valid actions for stop button
                mActionPanel.updateStatus(mGame);

                // Update bubble if moved at least once
                if (mGame.canStopMoving())
                    mPlayersPanel.showBubble(player, "I have " + remainingMoves + " move" + (remainingMoves != 1 ? "s" : "") + " remaining");

                String direction = readCommand("Choose direction (remaining: " + remainingMoves + ")", validCommands)[0];
                if (direction == null) {
                    continue;
                }

                // Stop moving
                if (direction.equals(COMMAND_STOP)) {
                    mGame.stopMoving();
                    break;
                }

                // Get tile for direction
                switch (direction.toLowerCase()) {
                    case COMMAND_LEFT:
                        moveTile = currentTile.getLeft();
                        break;
                    case COMMAND_UP:
                        moveTile = currentTile.getUp();
                        break;
                    case COMMAND_RIGHT:
                        moveTile = currentTile.getRight();
                        break;
                    case COMMAND_DOWN:
                        moveTile = currentTile.getDown();
                        break;
                }

                Location targetLocation = Location.fromTile(moveTile);
                if (targetLocation.isRoom()) {
                    // Ensure that player is not returning to the room that they started in
                    if (targetLocation == mGame.getTurnInitialPlayerRoom()) {
                        System.out.println("Can't return to same room");
                        continue;
                    }

                    mGame.moveTo(targetLocation);
                    break;
                } else {
                    currentTile = targetLocation.asTile();
                }

                mGame.moveTo(targetLocation);
            }
        }

        List<String> bubbleLines = new ArrayList<>();
        if (!mGame.getCurrentPlayerLocation().isRoom()) {
            bubbleLines.add("I couldn't make it in to a room, this investigation will have to wait");
            bubbleLines.add("Those dice are out to get me, now I'm stuck in the corridors..");
            bubbleLines.add("Looks like I won't be doing any investigating right now, let's see what the others come up with.");
            bubbleLines.add("Well, this certainly isn't getting me any closer figuring this murder out...");
            bubbleLines.add("Could the murder have been done out here? Surely not, there's too many people walking around.");
            bubbleLines.add("I may not have figured out this murder just yet, but I'll have it next time!");
        } else {
            Room room = mGame.getCurrentPlayerLocation().asRoom();
            if (room.isGuessRoom()) {
                bubbleLines.add("I think I know who committed the murder!! It was...");
                bubbleLines.add("I have concluded my investigations and am ready to announce the murderer. It was...");
                bubbleLines.add("If my calculations are correct, the murderer was...");
                bubbleLines.add(player.getCharacter().getName() + ", the master detective, has solved the murder! It was...");
                bubbleLines.add("I'm not quite sure, but I'll take a guess. The murderer was...");
                bubbleLines.add("Everyone!! I have it! The murder was done by...");
            } else {
                String roomName = room.getName().toLowerCase();
                String playerName = player.getCharacter().getName();
                switch (roomName) {
                    case "kitchen":
                        if (!playerName.equals("Mrs. White")) {
                            bubbleLines.add("Mrs. White is normally cooking in here, I wonder if she was involved..");
                        }
                        bubbleLines.add("A a meal time murder? At least he would have been full... Let's ask about these knives!");
                        bubbleLines.add("I'm getting awfully hungry, but this murder must be solved.. On to the questions!");
                        bubbleLines.add("These knives are very sharp, but they're all clean. I'll have to take a closer look");
                        break;
                    case "living room":
                        bubbleLines.add("This fireplace is nice and warm.. but so is that fire poker!!");
                        bubbleLines.add("I heard he would sometimes fall asleep on the couch, a perfect opportunity for the murderer!");
                        break;
                    case "billiard room":
                        bubbleLines.add("A game of billiards gone wrong? Perhaps someone is very competitive...");
                        bubbleLines.add("That ice pick on the ground seems very suspicious, I'm going to need some answers");
                        break;
                    case "trophy room":
                        if (!playerName.equals("Colonel Mustard")) {
                            bubbleLines.add("He always talked about his trophies with Colonel Mustard.. He must know something!");
                        }
                        bubbleLines.add("A murder in the trophy room? Someone must have been jealous...");
                        bubbleLines.add("Was someone jealous of all his trophies? I certainly am, but not _that_ jealous...");
                        break;
                    case "conservatory":
                        if (!player.getCharacter().getName().equals("Reverend Green")) {
                            bubbleLines.add("I heard Reverend Green does some gardening here, I wonder if he was involved..");
                        }
                        bubbleLines.add("These garden shears are very sharp, could they have been the murder weapon?");
                        break;
                    case "studio":
                        if (!player.getCharacter().getName().equals("Mrs. Peacock")) {
                            bubbleLines.add("Mrs. Peacock loves painting in here, let's ask her what she knows");
                        }
                        bubbleLines.add("I heard he kept a revolver in one of the desk drawers..");
                        break;
                    case "bedroom":
                        bubbleLines.add("The poor man, I hope he was asleep when it happened...");
                        bubbleLines.add("I've never been in here before, there's a lot to be discovered.. Let's ask some questions");
                        break;
                    case "hall":
                        bubbleLines.add("I heard they had to use rat poison in here, I wonder where they kept it..");
                        bubbleLines.add("Did someone push him down the stairs?");
                        break;
                    case "library":
                        if (!playerName.equals("Professor Plum")) {
                            bubbleLines.add("Professor Plum is always in here, he's going to have to give me some answers!");
                        }
                        bubbleLines.add("That candlestick seems out of place.. why is it at the edge of the table?!");
                        break;
                }

                // Add only 1 of the generic lines
                if (sRandom.nextBoolean()) {
                    bubbleLines.add("It's time for some questioning in the " + roomName + "!");
                } else {
                    bubbleLines.add("The " + roomName + " could give me some important clues, let's ask some questions");
                }
            }
        }

        mPlayersPanel.showBubble(player, bubbleLines.get(sRandom.nextInt(bubbleLines.size())));

        setPathFindingEnabled(false);
    }

    private void performQuestion(Player player, String[] args) {
        // Can only question in a room
        if (!mGame.getCurrentPlayerLocation().isRoom()) {
            return;
        }

        Suggestion suggestion;
        if (args.length == 1) {
            suggestion = createSuggestion(mGame.getCurrentPlayerLocation().asRoom());
        } else {
            if (args.length != 3) {
                System.out.println("Invalid number of arguments provided for question command");
                System.out.println(HELP_COMMANDS.get(COMMAND_QUESTION));
                return;
            }

            suggestion = createSuggestion(args[1], args[2], null, mGame.getCurrentPlayerLocation().asRoom());
        }

        if (suggestion == null) {
            return;
        }

        if (mGame.getUndistributedCards().contains(suggestion.suspect) || mGame.getUndistributedCards().contains(suggestion.weapon)) {
            System.out.println("Can't use undistributed cards in questions as they are visible to all players");
            return;
        }

        Player matchingPlayer = mGame.poseQuestion(suggestion);

        mOutputPanel.clear();
        System.out.println(player.getName() + " suggested " + suggestion.asHumanReadableString());

        if (matchingPlayer != null) {
            System.out.println(matchingPlayer.getName() + " has one of the suggestion cards");

            List<Card> matchingCards = matchingPlayer.matchingSuggestionCards(suggestion);

            // Hide all panels that could reveal extra info
            mPlayerCardsPanel.slideOut();
            mNotesSlideOutPanel.slideOut();
            mHistorySlideOutPanel.slideOut();

            // Show response text bubbles
            mPlayersPanel.showQuestionResponses(player, suggestion, matchingPlayer, () -> mInputPanel.append(COMMAND_SHOW));

            // Request that game is passed to matching player to show a card (text or UI)
            readCommand("Pass to " + matchingPlayer.getName() + " to show a card", COMMAND_SHOW);

            // Hide all panels again in case user opened them while waiting to show
            mPlayerCardsPanel.slideOut();
            mNotesSlideOutPanel.slideOut();
            mHistorySlideOutPanel.slideOut();

            // Pass to player
            passToPlayer(matchingPlayer, "temporarily");

            // Let matching player choose card to show
            Card shownCard = CardPickerDialog.showCardPickerDialog(mMainFrame, mBoardLayeredPane, matchingCards);

            // Report shown card to game
            mGame.questionResponse(shownCard);

            // Request that game is passed back to current player
            passToPlayer(player, "back");

            // Show response card to current player
            CardPickerDialog.showCardResponseDialog(mMainFrame, mBoardLayeredPane, matchingPlayer, shownCard);
            System.out.println(matchingPlayer.getName() + " has " + shownCard.getName());

            // Hide response text bubbles
            mPlayersPanel.hideBubbles();
        } else {
            mPlayersPanel.showQuestionResponses(player, suggestion, null, null);

            System.out.println("No players have any of the suggested cards");
        }

        // Update notes panel
        mNotesPanel.update();

        // Update history panel
        mHistoryPanel.update();
        mHistorySlideOutPanel.setVisible(true);
        mHistorySlideOutPanel.reposition();
    }

    private boolean performAccuse(Player player, String[] args) {
        Suggestion suggestion;
        if (args.length == 1) {
            suggestion = createSuggestion(null);
        } else {
            if (args.length != 4) {
                System.out.println("Invalid number of arguments provided for question command");
                System.out.println(HELP_COMMANDS.get(COMMAND_ACCUSE));
                return false;
            }

            suggestion = createSuggestion(args[1], args[2], args[3], null);
        }

        if (suggestion == null) {
            return false;
        }

        mPlayersPanel.showBubble(player, "It was... " + suggestion.asHumanReadableString() + "!");

        boolean correct = mGame.makeFinalAccusation(suggestion);

        mCardAnimationsPanel.finalAccusation(suggestion, mGame.getSolution());

        List<String> bubbleLines = new ArrayList<>();

        if (correct) {
            System.out.println("Congratulations! You were correct!");

            bubbleLines.add("My suspicions were correct!");
            bubbleLines.add("I knew I was right!");
            bubbleLines.add("I knew it all along!");
            bubbleLines.add("Case closed, mystery solved.");
        } else {
            System.out.println("Your guess was incorrect. You have been eliminated.");

            bubbleLines.add("Oh dear, looks like I got something wrong");
            bubbleLines.add("That's embarrassing.. I'll leave this one for somebody else to solve...");
            bubbleLines.add("Oops, that didn't go as planned");

            // Eliminate player
            mPlayersPanel.setPlayerEliminated(player, true);
        }

        mPlayersPanel.showBubble(player, bubbleLines.get(sRandom.nextInt(bubbleLines.size())));

        // Update history panel
        mHistoryPanel.update();
        mHistorySlideOutPanel.setVisible(true);
        mHistorySlideOutPanel.reposition();

        return correct;
    }

    private void performLog() {
        if (mGame.getLog().isEmpty()) {
            System.out.println("No entries in log");
        } else {
            mHistorySlideOutPanel.slideIn();
        }
    }

    private void performNotes() {
        mNotesSlideOutPanel.slideIn();
    }

    private void setClickAction(Runnable clickAction, Component clickArea, Cursor cursor) {
        if (mClickAction != null) {
            mBoardCursorPanel.removeMouseListener(mClickAction);
            mClickAction = null;
        }

        if (clickArea != null) {
            mBoardCursorPanel.setBounds(clickArea.getBounds());
        } else {
            mBoardCursorPanel.setBounds(0, 0, 0, 0);
        }

        if (cursor != null) {
            mBoardCursorPanel.setCursor(cursor);
        }

        if (clickAction != null) {
            mClickAction = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    clickAction.run();
                }
            };
            mBoardCursorPanel.addMouseListener(mClickAction);
        }
    }

    private void setPathFindingEnabled(boolean enabled) {
        mPathFindingEnabled = enabled;

        if (enabled) {
            for (Component c : mBoardTilePanel.getComponents()) c.addMouseListener(mTilePathFindingListener);
        } else {
            for (Component c : mBoardTilePanel.getComponents()) c.removeMouseListener(mTilePathFindingListener);
        }

        resetPathFindingTemporaryState();
    }

    private void resetPathFindingTemporaryState() {
        // Reset backgrounds of tiles in previous path
        if (mPreviousPath != null) {
            for (Tile tile : mPreviousPath) {
                tile.getButton().setPathFindingColors(false, false, false);
            }

            mPreviousPath = null;
        }

        setClickAction(null, null, null);
    }

    private void onTileHover(TileComponent tileComponent) {
        if (!mPathFindingEnabled) return;

        // Only perform path finding on valid tiles
        Tile tile = tileComponent.getTile();
        if (!(tile instanceof CorridorTile || tile instanceof RoomTile)) {
            resetPathFindingTemporaryState();
            return;
        }

        Location targetLocation = Location.fromTile(tile);
        Location currentLocation = mGame.getCurrentPlayerLocation();

        // Can't move to same location, or back into the room that the player started in
        if (targetLocation == currentLocation || targetLocation == mGame.getTurnInitialPlayerRoom()) {
            resetPathFindingTemporaryState();
            return;
        }

        // Try to find a path to the target location
        List<TokenOccupiableTile> path = PathFinder.findShortestPathAdvanced(currentLocation, targetLocation, Integer.MAX_VALUE);
        if (path == null) {
            resetPathFindingTemporaryState();
            return;
        }

        // Reset backgrounds of tiles not in new path
        if (mPreviousPath != null) {
            for (TokenOccupiableTile t : mPreviousPath) {
                if (!path.contains(t)) {
                    t.getButton().setPathFindingColors(false, false, false);
                }
            }
        }
        mPreviousPath = path;

        // Update backgrounds of tiles in new path
        for (int i = 0; i < path.size(); i++) {
            // Colour valid tiles in path green, invalid tiles in red
            TileComponent button = path.get(i).getButton();
            boolean active = i == (path.size() - 1);
            button.setPathFindingColors(true, i <= mGame.getTurnRemainingMoves(), active);
        }

        // Tile click action
        boolean canMove = (path.size() - 1) <= mGame.getTurnRemainingMoves();
        if (canMove) {
            setClickAction(() -> {
                if (mGame.moveTo(targetLocation) == 0) {
                    setPathFindingEnabled(false);
                }

                // Interrupt text input
                mInputPanel.inject("\3");
            }, tileComponent, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setClickAction(null, tileComponent, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    /**
     * Opens a dialog and allows for players to be registered to the game.
     */
    private void setupPlayers() {
        boolean demoMode = DEMO_MODE;

        if (!DEMO_MODE) {
            List<Suspect> remainingSuspects = mGame.getBoard().getSuspectsModifiable();
            while (remainingSuspects.size() > 0) {
                Player player = CardPickerDialog.showPlayerPickerDialog(mMainFrame, mBoardLayeredPane, remainingSuspects);
                if (player == Player.DEMO_MODE) {
                    demoMode = true;
                    break;
                }
                if (player != null) {
                    mPlayers.add(player);
                    remainingSuspects.remove(player.getCharacter());
                } else if (mPlayers.size() >= 2) {
                    // Must have at least 2 players to play
                    break;
                }
            }
        }

        // Fill in players if in demo mode
        if (demoMode) {
            mPlayers.clear();
            List<Suspect> suspects = mGame.getBoard().getSuspects();
            for (Suspect suspect : suspects) {
                mPlayers.add(new Player(suspect, suspect.getName()));
            }
        }
    }

    /**
     * Requests that the game be passed to a player.
     *
     * @param p Player to pass to.
     * @param type One of "back", "temporarily", null
     */
    private void passToPlayer(Player p, String type) {
        JDialog dialog = new JDialog(mMainFrame, true);

        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createDashedBorder(Color.WHITE, 4, 5, 5, true),
                        BorderFactory.createEmptyBorder(Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25))));

        panel.add(new ImageComponent(p.getCharacter().getCardImage()));

        panel.add(Box.createRigidArea(new Dimension(0, Config.screenRelativeSize(25))));

        String text = "Pass ";
        if (type != null) {
            text += type + " ";
        }
        text += "to " + p.getName();

        JLabel label = new JLabel(text);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(20)));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, Config.screenRelativeSize(25))));

        JButton button = new JButton("Done");
        button.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(32)));
        panel.add(button);

        button.addActionListener((e) -> dialog.dispose());

        panel.setBackground(new Color(0, 0, 0, 144));

        dialog.getContentPane().setLayout(new GridBagLayout());
        dialog.getContentPane().add(panel);
        dialog.setBackground(new Color(0, 0, 0, 144));
        dialog.setSize(mBoardLayeredPane.getSize());
        dialog.setLocationRelativeTo(mBoardLayeredPane);

        dialog.setVisible(true);
    }

    /**
     * Creates a new {@code Suggestion} from user selection in a dialog.
     *
     * If {@code currentRoom} is provided then that room is used, otherwise room is prompted too.
     *
     * @param currentRoom Player's current room, used in the suggestion if not null.
     * @return Suggestion created by user, or null if cancelled.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    private Suggestion createSuggestion(Room currentRoom) {
        List<Suspect> suspects = mGame.getBoard().getSuspectsModifiable();
        List<Weapon> weapons = mGame.getBoard().getWeaponsModifiable();
        List<Room> rooms = mGame.getBoard().getRoomsModifiable();

        // Can't make suggestions using the universally known cards
        suspects.removeAll(mGame.getUndistributedCards());
        weapons.removeAll(mGame.getUndistributedCards());
        rooms.removeAll(mGame.getUndistributedCards());

        if (currentRoom != null) {
            return CardPickerDialog.showSuggestionPickerDialog(mMainFrame, mBoardLayeredPane, suspects, weapons, currentRoom);
        } else {
            return CardPickerDialog.showAccusationPickerDialog(mMainFrame, mBoardLayeredPane, suspects, weapons, rooms);
        }
    }

    /**
     * Creates a new {@code Suggestion} from user text inputs.
     *
     * If {@code currentRoom} is provided then that room is used, otherwise {@code requestedRoom} is searched for.
     *
     * @param requestedSuspect the requested suspect
     * @param requestedWeapon the requested weapon
     * @param requestedRoom the requested room
     * @param currentRoom Player's current room, used in the suggestion if not null.
     * @return Suggestion created, or null if invalid.
     */
    private Suggestion createSuggestion(String requestedSuspect, String requestedWeapon, String requestedRoom, Room currentRoom) {
        List<Suspect> suspects = mGame.getBoard().getSuspects();
        List<Weapon> weapons = mGame.getBoard().getWeapons();
        List<Room> rooms = mGame.getBoard().getRooms();

        Suspect suspect = suspects.stream()
                .filter((s) -> Arrays.stream(s.getSearchNames()).anyMatch(requestedSuspect::equalsIgnoreCase))
                .findFirst().orElse(null);

        Weapon weapon = weapons.stream()
                .filter((w) -> Arrays.stream(w.getSearchNames()).anyMatch(requestedWeapon::equalsIgnoreCase))
                .findFirst().orElse(null);

        Room room = currentRoom != null ? currentRoom :
                rooms.stream()
                        .filter((r) -> Arrays.stream(r.getSearchNames()).anyMatch(requestedRoom::equalsIgnoreCase))
                        .findFirst().orElse(null);

        if (suspect == null) {
            System.out.println("Invalid suspect " + requestedSuspect);
        }
        if (weapon == null) {
            System.out.println("Invalid weapon " + requestedWeapon);
        }
        if (room == null) {
            System.out.println("Invalid room " + requestedRoom);
        }

        if (suspect != null && weapon != null && room != null) {
            return new Suggestion(suspect, weapon, room);
        } else {
            return null;
        }
    }

    /**
     * Initializes main UI
     */
    private void setupFrame() {
        mMainFrame = new JFrame();
        mMainFrame.setTitle(Config.TITLE);
        mMainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mMainFrame.setResizable(false);
        mMainFrame.setUndecorated(true);
        mMainFrame.setVisible(true);

        mMainFrame.getContentPane().setLayout(new BorderLayout());

        JPanel terminal = new JPanel();
        terminal.setLayout(new BoxLayout(terminal, BoxLayout.Y_AXIS));
        terminal.setPreferredSize(new Dimension(Config.screenWidthPercentage(0.3f), 0));

        mOutputPanel = new OutputPanel();
        mInputPanel = new InputPanel();

        JScrollPane outputScrollPane = new JScrollPane(mOutputPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        terminal.add(outputScrollPane);
        terminal.add(mInputPanel);

        System.setOut(new PrintStream(mOutputPanel.getOutputStream()));
        System.setIn(mInputPanel.getInputStream());

        mMainFrame.add(terminal, BorderLayout.LINE_END);

        setupBoard();

        mMainFrame.pack();
        mMainFrame.setLocationRelativeTo(null);
    }

    /**
     * Initializes board UI
     */
    private void setupBoard() {
        // Setup layered pane container
        mBoardLayeredPane = new JLayeredPane();
        mBoardLayeredPane.setBackground(TileComponent.COLOR_EMPTY);
        mBoardLayeredPane.setOpaque(true);
        mMainFrame.add(mBoardLayeredPane, BorderLayout.CENTER);

        mMainFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mMainFrame.requestFocus();
                mMainFrame.requestFocusInWindow();
            }
        });

        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(mMainFrame.getGraphicsConfiguration());
        int availableHeight = screenHeight - screenInsets.top - screenInsets.bottom;

        mTileSize = availableHeight / Config.Board.HEIGHT;

        Dimension boardDimension = new Dimension(mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT);
        mBoardLayeredPane.setPreferredSize(new Dimension(boardDimension.width, availableHeight));

        mBoardLayeredPane.setBorder(BorderFactory.createEmptyBorder(availableHeight - boardDimension.height, 0, 0, 0));

        Rectangle boardBounds = new Rectangle(0, 0, boardDimension.width, boardDimension.height);

        // Add tile panel
        mBoardTilePanel = new JPanel();
        mBoardTilePanel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));
        mBoardTilePanel.setBounds(boardBounds);
        mBoardTilePanel.setOpaque(false);

        mBoardLayeredPane.add(mBoardTilePanel, BOARD_LAYER_TILES);

        // Add room labels
        for (Room r : mGame.getBoard().getRoomsWithGuessRoom()) {
            JLabel label = new JLabel(r.getName().toUpperCase());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, mTileSize * 3 / 5));

            int centerX = (int) (r.getCenterX() * mTileSize);
            int centerY = (int) (r.getCenterY() * mTileSize);

            label.setBounds(centerX - 250, centerY - mTileSize / 2, 500, mTileSize);

            mBoardLayeredPane.add(label, BOARD_LAYER_ROOM_NAMES);

            r.setLabel(label);
        }

        // Add suspect tokens
        for (Suspect s : mGame.getBoard().getSuspects()) {
            mBoardLayeredPane.add(new TokenComponent(s, mTileSize), BOARD_LAYER_TOKENS);
        }

        // Add weapon tokens
        for (Weapon w : mGame.getBoard().getWeapons()) {
            mBoardLayeredPane.add(new TokenComponent(w, mTileSize), BOARD_LAYER_TOKENS);
        }

        // Add tiles
        for (int y = 0; y < Config.Board.HEIGHT; y++) {
            for (int x = 0; x < Config.Board.WIDTH; x++) {
                Tile tile = mGame.getBoard().getTile(x, y);
                TileComponent component = new TileComponent(tile);
                tile.setButton(component);
                component.setSize(mTileSize, mTileSize);

                // Tile borders
                char[] bc = BoardModel.getTileBordersAndCorners(x, y);
                if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
                    component.setBorder(new TileBorder(bc));
                }

                // Add start tile background circle
                if (tile instanceof StartTile) {
                    mBoardLayeredPane.add(new StartTileCircle((StartTile) tile, mTileSize), BOARD_LAYER_START_TILE_CIRCLES);
                }

                mBoardTilePanel.add(component);
            }
        }

        int sidePanelWidth = (int) (1.8 * mTileSize);

        // Add dice panel
        mBoardDicePanel = new DicePanel();
        mBoardLayeredPane.add(mBoardDicePanel, BOARD_LAYER_DICE);
        mBoardDicePanel.setBounds(boardBounds);
        mBoardDicePanel.initPhysics();

        // Add cursor panel
        mBoardCursorPanel = new JPanel();
        mBoardLayeredPane.add(mBoardCursorPanel, BOARD_LAYER_CURSOR);
        mBoardCursorPanel.setOpaque(false);

        // Add action panel
        mActionPanel = new ActionPanel(mInputPanel::append, sidePanelWidth);
        mBoardLayeredPane.add(mActionPanel, BOARD_LAYER_ACTIONS);
        mActionPanel.setBounds(0, 0, sidePanelWidth, boardDimension.height);
        mActionPanel.updateStatus(mGame);

        // Add card distribution panel
        mCardAnimationsPanel = new CardAnimationsPanel();
        mBoardLayeredPane.add(mCardAnimationsPanel, BOARD_LAYER_CARDS);
        mCardAnimationsPanel.setLocation(sidePanelWidth, 0);
        mCardAnimationsPanel.setSize(boardDimension.width - sidePanelWidth, boardDimension.height);

        // Slide out panels
        int slideOutPanelHandleSize = Config.screenRelativeSize(40);
        int slideOutPanelHandleWidth = Config.screenHeightPercentage(0.2f);

        // Player cards slide out panel
        mPlayerCardsPanel = new SlideOutCardsPanel(SlideOutPanel.RIGHT,
                "Your cards".toUpperCase(),
                slideOutPanelHandleSize,
                slideOutPanelHandleWidth,
                boardDimension.width,
                boardDimension.height / 4 - slideOutPanelHandleWidth / 2);
        mBoardLayeredPane.add(mPlayerCardsPanel, BOARD_LAYER_PANELS);
        mPlayerCardsPanel.setVisible(false);

        // Undistributed cards slide out panel
        mUndistributedCardsPanel = new SlideOutCardsPanel(SlideOutPanel.RIGHT,
                "Public cards".toUpperCase(),
                slideOutPanelHandleSize,
                slideOutPanelHandleWidth,
                boardDimension.width,
                boardDimension.height * 3 / 4 - slideOutPanelHandleWidth / 2);
        mBoardLayeredPane.add(mUndistributedCardsPanel, BOARD_LAYER_PANELS);
        mUndistributedCardsPanel.setVisible(false);

        // Add quit panel
        SlideOutPanel quitPanel = new SlideOutCardsPanel(SlideOutPanel.TOP,
                "Quit".toUpperCase(),
                Config.screenRelativeSize(30),
                sidePanelWidth,
                boardDimension.width,
                boardDimension.width - sidePanelWidth + Config.screenRelativeSize(8));
        quitPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to quit?",
                        "Quit Cluedo?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        mBoardLayeredPane.add(quitPanel, BOARD_LAYER_PANELS);
        quitPanel.reposition();
    }

    private void setupBoardWithPlayers() {
        Dimension boardDimension = mBoardLayeredPane.getSize();
        Rectangle boardBounds = mBoardLayeredPane.getBounds();

        int sidePanelWidth = (int) (1.8 * mTileSize);

        // Add players panel
        mPlayersPanel = new PlayersPanel(mPlayers, sidePanelWidth);
        mBoardLayeredPane.add(mPlayersPanel, BOARD_LAYER_PLAYERS);
        mPlayersPanel.setBounds(boardBounds);

        // Slide out panels
        int slideOutPanelHandleSize = Config.screenRelativeSize(40);
        int slideOutPanelAvailableWidth = boardDimension.width - 2 * sidePanelWidth;
        int slideOutPanelHandleWidth = (slideOutPanelAvailableWidth - sidePanelWidth) / 2;

        // Log panel
        mHistoryPanel = new LogPanel(mPlayers, mGame.getLog());
        mHistorySlideOutPanel = new SlideOutPanel(SlideOutPanel.BOTTOM,
                "History".toUpperCase(),
                slideOutPanelHandleSize,
                slideOutPanelHandleWidth,
                boardDimension.height,
                sidePanelWidth,
                false);
        mHistorySlideOutPanel.setLayout(new GridBagLayout());
        mBoardLayeredPane.add(mHistorySlideOutPanel, BOARD_LAYER_PANELS);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        mHistorySlideOutPanel.add(mHistoryPanel, c);
        mHistorySlideOutPanel.setVisible(false);

        // Notes panel
        mNotesPanel = new NotesPanel(mPlayers, mGame.getBoard(), mGame.getUndistributedCards());
        mNotesSlideOutPanel = new SlideOutPanel(SlideOutPanel.BOTTOM,
                "Notes".toUpperCase(),
                slideOutPanelHandleSize,
                slideOutPanelHandleWidth,
                boardDimension.height,
                sidePanelWidth + slideOutPanelAvailableWidth - slideOutPanelHandleWidth,
                false);
        mBoardLayeredPane.add(mNotesSlideOutPanel, BOARD_LAYER_PANELS);
        mNotesSlideOutPanel.add(mNotesPanel);
        mNotesSlideOutPanel.setVisible(false);
    }

    private void dealCards() {
        if (DEMO_MODE) {
            return;
        }

        System.out.println("Dealing cards to players...");
        if (ALLOW_SKIPPING_ANIMATIONS) System.out.println("Click on board to skip");

        // Show cursor panel to allow force finishing dice roll
        if (ALLOW_SKIPPING_ANIMATIONS) setClickAction(mCardAnimationsPanel::forceFinish, mBoardTilePanel, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        mCardAnimationsPanel.dealCards(mPlayers.size(), mPlayersPanel.getItemHeight(), mGame.getUndistributedCards(), mGame.getBoard());

        // Hide cursor panel
        if (ALLOW_SKIPPING_ANIMATIONS) setClickAction(null, null, null);
    }

    private void throwDiceForOrder() {
        if (DEMO_MODE) {
            return;
        }

        System.out.println("Rolling dice to decide player order...");
        System.out.println("Click on board to skip");

        // Show cursor panel to allow force finishing dice roll
        setClickAction(() -> {
            mBoardDicePanel.forceFinish();
            Animator.getInstance().interruptAllAnimations(mBoardDicePanel);
        }, mBoardTilePanel, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Store list of players that are in the current round (highest rollers or all players initially)
        List<Player> currentRoundPlayers = new ArrayList<>(mPlayers);
        while (currentRoundPlayers.size() > 1) {
            // List of players that have rolled the highest number
            List<Player> highestRollPlayers = new ArrayList<>();

            // Current highest roll
            int highestRoll = 0;

            // Each player in current round rolls
            for (Player player : currentRoundPlayers) {
                // Current rolling player is highlighted
                mPlayersPanel.setActivePlayer(mPlayers.indexOf(player));

                // Roll dice
                int roll = mBoardDicePanel.rollDice(null, true);
                System.out.println(player.getName() + " rolled " + roll);

                if (roll > highestRoll) {
                    // New highest roll
                    highestRoll = roll;
                    highestRollPlayers.clear();
                    highestRollPlayers.add(player);
                } else if (roll == highestRoll) {
                    // Matching highest roll
                    highestRollPlayers.add(player);
                }

                // Show result bubble
                mPlayersPanel.showBubble(player, "I rolled " + roll);
            }

            mPlayersPanel.setActivePlayer(-1);

            // Wait for last player's bubble to show fully
            Animator.getInstance().animate(mBoardDicePanel)
                    .setDelay(1500)
                    .await();

            // Players with the highest roll must roll again
            if (highestRollPlayers.size() > 1) {
                String names = Util.implode(highestRollPlayers.stream().map(Player::getName).collect(Collectors.toList()), ", ", " and ");

                System.out.println(names + " " + (highestRollPlayers.size() > 2 ? "all" : "both") + " rolled " + highestRoll + ", they must now roll again!");
            }

            // Players not in next round are eliminated
            for (Player player : mPlayers) {
                if (!highestRollPlayers.contains(player)) {
                    mPlayersPanel.setPlayerEliminated(player, true);
                }
            }

            // Wait for bubbles to hide
            Animator.getInstance().animate(mBoardDicePanel)
                    .setDelay(1500)
                    .await();

            mPlayersPanel.hideBubbles();

            // Wait for bubbles to hide
            Animator.getInstance().animate(mBoardDicePanel)
                    .setDelay(1000)
                    .await();

            // Next round players are those that rolled the highest number
            currentRoundPlayers = highestRollPlayers;
        }

        // Place winning player at the top
        Player topPlayer = currentRoundPlayers.get(0);
        int topPlayerIndex = mPlayers.indexOf(topPlayer);
        mPlayersPanel.setTopPlayer(currentRoundPlayers.get(0));

        // Put at front of players
        List<Player> old = new ArrayList<>(mPlayers);
        for (int i = 0; i < old.size(); i++) {
            int newIndex = (i - topPlayerIndex + mPlayers.size()) % mPlayers.size();
            mPlayers.set(newIndex, old.get(i));
        }

        // Wait for rearrangement to complete
        Animator.getInstance().animate(mBoardDicePanel)
                .setDelay(2000)
                .await();

        // Players are not eliminated to start game
        for (Player player : mPlayers) {
            mPlayersPanel.setPlayerEliminated(player, false);
        }

        // Hide cursor panel
        setClickAction(null, null, null);
    }
}
