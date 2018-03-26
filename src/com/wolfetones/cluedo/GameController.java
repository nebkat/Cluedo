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
import com.wolfetones.cluedo.game.Suggestion;
import com.wolfetones.cluedo.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {
    private static final boolean DEMO_MODE = Boolean.parseBoolean(System.getProperty("debug"));

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
    private static final Integer BOARD_LAYER_CURSOR = 7;

    /**
     * Prefix placed in front of commands to hide from the list of valid commands
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

    private static final String COMMAND_YES = "y";
    private static final String COMMAND_NO = "n";

    /**
     * Game instance
     */
    private Game mGame = new Game();

    private List<Player> mPlayers = new ArrayList<>();

    private int mTileSize;

    /**
     * Swing containers
     */
    private JFrame mMainFrame;
    private JPanel mainPanel;

    private JLayeredPane mBoardLayeredPane;
    private JPanel mBoardTilePanel;
    private DicePanel mBoardDicePanel;
    private JPanel mBoardCursorPanel;

    private OutputPanel mOutputPanel;
    private InputPanel mInputPanel;
    private JTabbedPane mLeftPane;
    private JPanel mIOPanel;
    private boolean mNotesPanelActive = false;

    private PlayersPanel mPlayersPanel;
    private ActionPanel mActionPanel;

    /**
     * Path Finding
     */
    private boolean mPathFindingEnabled = false;
    private List<TileComponent> mTileComponents = new ArrayList<>();
    private List<TokenOccupiableTile> mPreviousPath;

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
        setupPlayers();
        setupFrame();

        throwDiceForOrder();

        mInputScanner = new Scanner(System.in);

        mPlayers.forEach(mGame::addPlayer);
        mGame.start();

        // Keep performing new turns until the game is over
        while (!mGame.isFinished()) {
            performTurn();
        }
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

        String[] command;
        String line;
        while (true) {
            line = mInputScanner.nextLine();

            // Interrupt/end of text
            if (line.equals("\3")) {
                return new String[] {null};
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

        mInputPanel.setCommandHints(null);
        mInputPanel.clear();

        return command;
    }

    /**
     * Performs all necessary steps for a player turn
     */
    private void performTurn() {
        Player player = mGame.nextTurn();

        mPlayersPanel.setActivePlayer(player);
        mPlayersPanel.hideBubbles();
        passToPlayer(player, null);

        mOutputPanel.clear();

        System.out.println(player.getName() + "'s move (" + player.getCharacter().getName() + ")");

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
            String[] args = readCommand("Choose action", commands);
            String command = args[0];
            mActionPanel.hideAllExceptDone();
            if (command == null) {
                // Ignore and continue
                System.err.println("Unexpected interrupt");
            } else if (command.equals(COMMAND_QUIT)) {
                System.out.println("The solution was: " + mGame.getSolution().asHumanReadableString());
                System.exit(0);
            } else if (command.equals(COMMAND_CHEAT)) {
                System.out.println("The solution is: " + mGame.getSolution().asHumanReadableString());
            } else if (command.equals(COMMAND_HELP)) {
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


            } else if (command.equals(COMMAND_DONE)) {
                break;
            } else if (command.equals(COMMAND_ROLL)) {
                int[] dice = new int[Game.NUM_DICE];
                mBoardDicePanel.rollDice(dice, false);

                // Game will read dice values from dice array if they are not 0
                int remainingMovements = mGame.rollDice(dice);

                mActionPanel.updateStatus(mGame);

                System.out.println("Rolled " + Util.implode(Arrays.stream(dice).boxed().collect(Collectors.toList()), "+") + " = " + remainingMovements);

                setPathFindingEnabled(true);

                // Exit if in room
                Location startLocation = mGame.getCurrentPlayerLocation();
                if (startLocation.isRoom()) {
                    List<RoomTile> corridorTiles = startLocation.asRoom().getEntranceCorridors();
                    corridorTiles.sort(Comparator.comparingInt(Tile::getX));
                    String[] validCommands = new String[corridorTiles.size()];
                    for (int i = 0; i < corridorTiles.size(); i++) {
                        validCommands[i] = Integer.toString(i + 1);
                    }
                    String entranceCommand = readCommand("Choose room exit (or use board tiles)", validCommands)[0];
                    // If not interrupted by path finding/UI
                    if (entranceCommand != null) {
                        int entranceCorridor = Integer.parseInt(entranceCommand) - 1;

                        mGame.moveTo(corridorTiles.get(entranceCorridor).getDoorTile());
                    }
                }

                // May have moved directly to another move using path finding/UI
                if (mGame.getTurnRemainingMoves() == 0) {
                    continue;
                }

                Tile moveTile = null;
                CorridorTile currentTile = mGame.getCurrentPlayerLocation().asTile();
                List<String> validCommands = new ArrayList<>(5);
                while (mGame.getTurnRemainingMoves() > 0) {
                    validCommands.clear();
                    if (currentTile == null) {
                        throw new IllegalStateException("Current tile cannot be null");
                    }
                    if (currentTile.canMoveLeft()) validCommands.add(COMMAND_LEFT);
                    if (currentTile.canMoveUp()) validCommands.add(COMMAND_UP);
                    if (currentTile.canMoveRight()) validCommands.add(COMMAND_RIGHT);
                    if (currentTile.canMoveDown()) validCommands.add(COMMAND_DOWN);
                    if (mGame.canStopMoving()) validCommands.add(HIDDEN_COMMAND_PREFIX + COMMAND_STOP);

                    mActionPanel.updateStatus(mGame);

                    String direction = readCommand("Choose direction (or use board tiles)", validCommands)[0];
                    if (direction == null) {
                        continue;
                    }
                    if (direction.equals(COMMAND_STOP)) {
                        mGame.stopMoving();
                        break;
                    }

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

                    if (moveTile instanceof RoomTile) {
                        if (((RoomTile) moveTile).getRoom() == startLocation) {
                            System.out.println("Can't return to same room");
                            continue;
                        }

                        mGame.moveTo(((RoomTile) moveTile).getRoom());
                        break;
                    } else {
                        currentTile = (CorridorTile) moveTile;
                    }

                    mGame.moveTo(currentTile);
                }

                setPathFindingEnabled(false);
            } else if (command.equals(COMMAND_PASSAGE)) {
                mGame.usePassage();
            } else if (command.equals(COMMAND_QUESTION)) {
                Suggestion suggestion = createSuggestion(mGame.getCurrentPlayerLocation().asRoom());

                if (suggestion == null) {
                    continue;
                }

                Player matchingPlayer = mGame.poseQuestion(suggestion);

                if (matchingPlayer != null) {
                    List<Card> matchingCards = matchingPlayer.matchingSuggestionCards(suggestion);

                    mPlayersPanel.showQuestionResponses(player, suggestion, matchingPlayer, () -> mInputPanel.append(COMMAND_SHOW));
                    readCommand("Pass to " + matchingPlayer.getName() + " to show a card", COMMAND_SHOW);

                    passToPlayer(matchingPlayer, "temporarily");

                    Card shownCard = CardPickerDialog.showCardPickerDialog(mMainFrame, matchingCards);
                    player.addKnowledge(shownCard);

                    passToPlayer(player, "back");

                    CardPickerDialog.showCardResponseDialog(mMainFrame, matchingPlayer, shownCard);
                    System.out.println(matchingPlayer.getName() + " has " + shownCard.getName());

                    mPlayersPanel.hideBubbles();
                } else {
                    mPlayersPanel.showQuestionResponses(player, suggestion, null, null);

                    System.out.println("No players have any of the suggested cards");
                }
            } else if (command.equals(COMMAND_ACCUSE)) {
                Suggestion suggestion = createSuggestion(null);
                if (suggestion == null) {
                    continue;
                }

                boolean correct = mGame.makeFinalAccusation(suggestion);

                if (correct) {
                    System.out.println("Congratulations! You were correct!");
                } else {
                    System.out.println("Your guess was incorrect. You have been eliminated.");
                    mPlayersPanel.setPlayerEliminated(player, true);
                }
            } else if (command.equals(COMMAND_NOTES)) {
                JFrame tmpNotesFrame =  new JFrame();
                tmpNotesFrame.add(new NotesPanel(player, mGame.getBoard().getSuspects(), mGame.getBoard().getWeapons(), mGame.getBoard().getRooms(), mGame.getRemainingCards()));
                tmpNotesFrame.pack();
                tmpNotesFrame.setVisible(true);
            } else if (command.equals(COMMAND_LOG)) {
                List<Game.LogEntry> log = mGame.getLog();

                if (log.isEmpty()) {
                    System.out.println("No entries in log");
                }

                for (int i = 0; i < log.size(); i++) {
                    Game.LogEntry entry = log.get(i);
                    String text = (i + 1) + ". ";
                    if (entry.player == player) {
                        text += "You";
                    }
                    if (entry.type == Game.LogEntry.Type.Question) {
                        text += " suggested " + entry.suggestion.asHumanReadableString() + ", ";
                        if (entry.responder != null) {
                            if (entry.responder == player) {
                                text += "you";
                            } else {
                                text += entry.responder.getName();
                            }
                            text += " showed a card";
                        } else {
                            text += "nobody had any card";
                        }
                    } else if (entry.type == Game.LogEntry.Type.FinalAccusation) {
                        text += " made a final accusation of " + entry.suggestion.asHumanReadableString() + " ";
                        String was = entry.player == player ? "were" : "was";
                        if (entry.correct) {
                            text += "and " + was + " correct";
                        } else {
                            text += "but" + was + " incorrect";
                        }
                    }

                    System.out.println(text);
                }
            }
        }
    }

    private void setCursor(Cursor cursor, JComponent component) {
        if (cursor == null) {
            mBoardCursorPanel.setBounds(0, 0, 0, 0);
        } else {
            mBoardCursorPanel.setCursor(cursor);
            mBoardCursorPanel.setBounds(component.getBounds());
        }
    }

    private void setPathFindingEnabled(boolean enabled) {
        mPathFindingEnabled = enabled;

        resetPathFindingTemporaryState();
    }

    private void resetPathFindingTemporaryState() {
        if (mPreviousPath != null) {
            for (Tile tile : mPreviousPath) {
                tile.getButton().setTemporaryBackground(null);
            }

            mPreviousPath = null;
        }
    }

    private void onTileClick(TileComponent tileComponent) {
        if (!mPathFindingEnabled) return;

        // Only perform path finding on valid tiles
        Tile tile = tileComponent.getTile();
        if (!(tile instanceof CorridorTile || tile instanceof RoomTile)) {
            return;
        }

        Location targetLocation = Location.fromTile(tile);
        Location currentLocation = mGame.getCurrentPlayerLocation();

        List<TokenOccupiableTile> path = PathFinder.findShortestPathAdvanced(currentLocation, targetLocation, mGame.getTurnRemainingMoves());
        if (path == null) {
            return;
        }

        if (mGame.moveTo(targetLocation) == 0) {
            setPathFindingEnabled(false);
        }

        // Interrupt text input
        mInputPanel.inject("\3");
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

        List<TokenOccupiableTile> path = PathFinder.findShortestPathAdvanced(currentLocation, targetLocation, Integer.MAX_VALUE);
        if (path == null) {
            resetPathFindingTemporaryState();
            return;
        }

        if (mPreviousPath != null) {
            for (TokenOccupiableTile t : mPreviousPath) {
                if (!path.contains(t)) {
                    t.getButton().setTemporaryBackground(null);
                }
            }
        }
        mPreviousPath = path;

        boolean canMove = (path.size() - 1) <= mGame.getTurnRemainingMoves();
        setCursor(Cursor.getPredefinedCursor(canMove ? Cursor.HAND_CURSOR : Cursor.CROSSHAIR_CURSOR),
                path.get(path.size() - 1).getButton());

        for (int i = 0; i < path.size(); i++) {
            // Colour valid tiles in path green, invalid tiles in red
            TileComponent button = path.get(i).getButton();
            boolean active = i == (path.size() - 1);
            Color color;
            if (i <= mGame.getTurnRemainingMoves()) {
                color = active ? TileComponent.COLOR_PATHFINDING_VALID_ACTIVE : TileComponent.COLOR_PATHFINDING_VALID;
            } else {
                color = active ? TileComponent.COLOR_PATHFINDING_INVALID_ACTIVE : TileComponent.COLOR_PATHFINDING_INVALID;
            }
            button.setTemporaryBackground(color);
        }
    }

    /**
     * Requests that the user choose a card from the list of cards provided.
     *
     * @param question Prompt to provide to the user when listing valid cards.
     * @param cards List of cards available.
     * @param <T> {@code Room}, {@code Suspect} or {@code Weapon}.
     * @return Card chosen by the user.
     */
    private <T extends Card> T chooseCard(String question, List<T> cards) {
        List<String> names = cards.stream().map(Card::getShortName).collect(Collectors.toList());
        Map<String, T> map = cards.stream().collect(Collectors.toMap(Card::getShortName, Function.identity()));

        String card = readCommand(question, names)[0];
        return map.get(card);
    }

    /**
     * Opens a dialog and allows for players to be registered to the game.
     */
    private void setupPlayers() {
        if (DEMO_MODE) {
            for (int i = 0; i < 6; i++) {
                Suspect suspect = mGame.getBoard().getSuspect(i);
                mPlayers.add(new Player(suspect, suspect.getName()));
            }

            return;
        }

        List<Suspect> remainingSuspects = mGame.getBoard().getSuspects();

        while (remainingSuspects.size() > 0) {
            Player player = CardPickerDialog.showPlayerPickerDialog(null, remainingSuspects);
            if (player != null) {
                mPlayers.add(player);
                remainingSuspects.remove(player.getCharacter());
            } else if (mPlayers.size() >= 2) {
                // Must have at least 2 players to play
                break;
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
                        BorderFactory.createDashedBorder(Color.BLACK, 4, 5, 5, true),
                        BorderFactory.createEmptyBorder(Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25),
                                Config.screenRelativeSize(25))));

        panel.add(new ScaledImageComponent(p.getCharacter().getCardImage()));

        panel.add(Box.createRigidArea(new Dimension(0, Config.screenRelativeSize(25))));

        String text = "Pass ";
        if (type != null) {
            text += type + " ";
        }
        text += "to " + p.getName();

        JLabel label = new JLabel(text);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(20)));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, Config.screenRelativeSize(25))));

        JButton button = new JButton("Done");
        button.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(32)));
        panel.add(button);

        button.addActionListener((e) -> dialog.dispose());

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(mMainFrame);

        dialog.setVisible(true);
    }

    /**
     * Creates a new {@code Suggestion} from user selection in a dialog.
     *
     * If {@code currentRoom} is provided then that room is used, otherwise room is prompted too.
     *
     * @param currentRoom Player's current room, used in the suggestion if not null.
     * @return Suggestion created by user.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    private Suggestion createSuggestion(Room currentRoom) {
        List<Suspect> suspects = mGame.getBoard().getSuspects();
        List<Weapon> weapons = mGame.getBoard().getWeapons();
        List<Room> rooms = mGame.getBoard().getRooms();

        // Can't make suggestions using the universally known cards
        suspects.removeAll(mGame.getRemainingCards());
        weapons.removeAll(mGame.getRemainingCards());
        rooms.removeAll(mGame.getRemainingCards());

        if (currentRoom != null) {
            return CardPickerDialog.showSuggestionPickerDialog(mMainFrame, suspects, weapons, currentRoom);
        } else {
            return CardPickerDialog.showAccusationPickerDialog(mMainFrame, suspects, weapons, rooms);
        }

        /*Suspect suspect = chooseCard("Choose suspect", suspects);
        Weapon weapon = chooseCard("Choose weapon", weapons);
        Room room = currentRoom == null ? chooseCard("Choose room", rooms) : currentRoom;

        return new Suggestion(room, suspect, weapon);*/
    }

    /**
     * Initializes main UI
     */
    private void setupFrame() {
        mMainFrame = new JFrame();
        mMainFrame.setTitle(Config.TITLE);
        mMainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mMainFrame.setResizable(false);

        mTileSize = Config.screenHeightPercentage(0.9f) / Config.Board.HEIGHT;

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mMainFrame.setContentPane(mainPanel);

        mIOPanel = new JPanel();
        mIOPanel.setLayout(new BoxLayout(mIOPanel, BoxLayout.Y_AXIS));
        mIOPanel.setPreferredSize(new Dimension(Config.screenWidthPercentage(0.25f), 0));

        mOutputPanel = new OutputPanel();
        mInputPanel = new InputPanel();

        JScrollPane outputScrollPane = new JScrollPane(mOutputPanel);
        outputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        mIOPanel.add(outputScrollPane);
        mIOPanel.add(mInputPanel);

        System.setOut(new PrintStream(mOutputPanel.getOutputStream()));
        System.setIn(mInputPanel.getInputStream());

        //mIOPanel = new NotesPanel(mPlayers.get(0), mGame.getBoard().getSuspects(), mGame.getBoard().getWeapons(), mGame.getBoard().getRooms(), mGame.getRemainingCards());

        mMainFrame.add(mIOPanel, BorderLayout.LINE_END);

        setupBoard();

        mMainFrame.pack();
        mMainFrame.setLocationRelativeTo(null);
        mMainFrame.setVisible(true);
    }

    /**
     * Toggles the left panel between the I/O terminal and the notes screen
     * @param player Player whose notes should be displayed
     */
    public void toggleNotesPanel(Player player) {
        if (mNotesPanelActive) {
            mNotesPanelActive = false;
        } else {
            //mLeftPanel = new NotesPanel(player, mGame.getBoard().getSuspects(), mGame.getBoard().getWeapons(), mGame.getBoard().getRooms(), mGame.getRemainingCards());
            mMainFrame.removeAll();
            mMainFrame.setContentPane(mainPanel);
            mMainFrame.add(new NotesPanel(player, mGame.getBoard().getSuspects(), mGame.getBoard().getWeapons(), mGame.getBoard().getRooms(), mGame.getRemainingCards()));

            mNotesPanelActive = true;
            mMainFrame.revalidate();
        }
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

        Dimension boardDimension = new Dimension(mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT);
        mBoardLayeredPane.setPreferredSize(boardDimension);

        Rectangle boardBounds = new Rectangle(0, 0, boardDimension.width, boardDimension.height);

        // Add tile panel
        mBoardTilePanel = new JPanel();
        mBoardTilePanel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));
        mBoardTilePanel.setBounds(boardBounds);
        mBoardTilePanel.setOpaque(false);

        mBoardLayeredPane.add(mBoardTilePanel, BOARD_LAYER_TILES);

        // Add room labels
        for (Room r : mGame.getBoard().getRooms()) {
            JLabel label = new JLabel(r.getName().toUpperCase());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Config.screenRelativeSize(20)));

            int centerX = (int) (r.getCenterX() * mTileSize);
            int centerY = (int) (r.getCenterY() * mTileSize);

            label.setBounds(centerX - 250, centerY - 100, 500, 200);

            mBoardLayeredPane.add(label, BOARD_LAYER_ROOM_NAMES);
        }

        // Add suspect tokens
        for (Suspect s : mGame.getBoard().getSuspects()) {
            mBoardLayeredPane.add(new SuspectTokenComponent(s, mTileSize), BOARD_LAYER_TOKENS);
        }

        // Add weapon tokens
        for (Weapon w : mGame.getBoard().getWeapons()) {
            mBoardLayeredPane.add(new WeaponTokenComponent(w, mTileSize), BOARD_LAYER_TOKENS);
        }

        // Add tiles
        for (int y = 0; y < Config.Board.HEIGHT; y++) {
            for (int x = 0; x < Config.Board.WIDTH; x++) {
                Tile tile = mGame.getBoard().getTile(x, y);
                TileComponent component = new TileComponent(tile);
                tile.setButton(component);
                component.setSize(mTileSize, mTileSize);

                mTileComponents.add(component);

                component.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onTileClick(component);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        onTileHover(component);
                    }
                });

                // Tile borders
                char[] bc = BoardModel.getTileBordersAndCorners(x, y);
                if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
                    component.setBorder(new TileBorder(bc));
                }

                // Add start tile background circle
                if (tile instanceof StartTile) {
                    mBoardLayeredPane.add(new StartTileCircle((StartTile) tile, mTileSize), BOARD_LAYER_START_TILE_CIRCLES);
                }

                // Set colors
                if (tile instanceof PassageTile) {
                    component.setBackground(TileComponent.COLOR_PASSAGE);
                } else if (tile instanceof RoomTile) {
                    component.setBackground(TileComponent.COLOR_ROOM);
                } else if (tile instanceof CorridorTile) {
                    if ((y + x) % 2 == 0) {
                        component.setBackground(TileComponent.COLOR_CORRIDOR_A);
                    } else {
                        component.setBackground(TileComponent.COLOR_CORRIDOR_B);
                    }
                } else if (tile instanceof EmptyTile) {
                    component.setOpaque(false);
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

        // Add players panel
        mPlayersPanel = new PlayersPanel(Collections.unmodifiableList(mPlayers), sidePanelWidth);
        mBoardLayeredPane.add(mPlayersPanel, BOARD_LAYER_PLAYERS);
        mPlayersPanel.setBounds(boardBounds);

        // Add action panel
        mActionPanel = new ActionPanel(mInputPanel::append, sidePanelWidth);
        mBoardLayeredPane.add(mActionPanel, BOARD_LAYER_ACTIONS);
        mActionPanel.setBounds(boardBounds);
        mActionPanel.updateStatus(mGame);
    }

    private void throwDiceForOrder() {
        if (DEMO_MODE) {
            return;
        }

        System.out.println("Rolling dice to decide player order...");
        System.out.println("Click on board to skip");

        // Show cursor panel to allow force finishing dice roll
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), mBoardTilePanel);
        MouseAdapter interruptDiceClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mBoardDicePanel.forceFinish();
            }
        };
        mBoardCursorPanel.addMouseListener(interruptDiceClickListener);

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
                mPlayersPanel.setActivePlayer(player);

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
                mPlayersPanel.showDiceRollResult(player, roll);
            }

            mPlayersPanel.setActivePlayer(null);

            // Wait for last player's bubble to show fully
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // Ignore
            }

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
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // Ignore
            }

            mPlayersPanel.hideBubbles();

            // Wait for bubbles to hide
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }

            // Next round players are those that rolled the highest number
            currentRoundPlayers = highestRollPlayers;
        }

        // Sort and rearrange players by their result
        Player p;
        while ((p = mPlayers.get(0)) != currentRoundPlayers.get(0)) {
            mPlayers.remove(p);
            mPlayers.add(p);
        }
        mPlayersPanel.rearrangePlayers(Collections.unmodifiableList(mPlayers));

        // Wait for rearrangement to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Players are not eliminated to start game
        for (Player player : mPlayers) {
            mPlayersPanel.setPlayerEliminated(player, false);
        }

        // Hide cursor panel
        setCursor(null, null);
        mBoardCursorPanel.removeMouseListener(interruptDiceClickListener);
    }
}
