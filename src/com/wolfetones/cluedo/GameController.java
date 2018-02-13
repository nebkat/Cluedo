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
import com.wolfetones.cluedo.game.Location;
import com.wolfetones.cluedo.game.Player;
import com.wolfetones.cluedo.game.Suggestion;
import com.wolfetones.cluedo.ui.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {
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

    /**
     * Prefix placed in front of commands to hide from the list of valid commands
     *
     * Used with commands such as quit and cheat which are always available
     * but not necessary to be shown every time.
     */
    private static final String HIDDEN_COMMAND_PREFIX = "@";

    /**
     * Commands that can be executed by the user
     */
    private static final String COMMAND_ROLL = "roll";
    private static final String COMMAND_PASSAGE = "passage";
    private static final String COMMAND_DONE = "done";
    private static final String COMMAND_QUIT = "quit";
    private static final String COMMAND_QUESTION = "question";
    private static final String COMMAND_ACCUSE = "accuse";
    private static final String COMMAND_NOTES = "notes";
    private static final String COMMAND_LOG = "log";
    private static final String COMMAND_CHEAT = "cheat";

    private static final String COMMAND_LEFT = "l";
    private static final String COMMAND_UP = "u";
    private static final String COMMAND_RIGHT = "r";
    private static final String COMMAND_DOWN = "d";
    private static final String COMMAND_STOP = "stop";

    private static final String COMMAND_YES = "y";
    private static final String COMMAND_NO = "n";

    /**
     * Game instance
     */
    private Game mGame = new Game();

    private int mTileSize;

    /**
     * Swing containers
     */
    private JFrame mMainFrame;

    private JLayeredPane mBoardLayeredPane;
    private JPanel mBoardTilePanel;

    private OutputPanel mOutputPanel;
    private InputPanel mInputPanel;

    /**
     * TODO: Temporary pathfinding test code
     */
    private static Location mEndLocation = null;
    private static Token mToken = null;
    private Timer mTimer;

    /**
     * Scanner for reading stdin
     */
    private Scanner mInputScanner;

    public static void main(String[] args) {
        System.out.println("Welcome to " + Config.TITLE + " by");
        System.out.println(Config.AUTHOR);

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

        mInputScanner = new Scanner(System.in);

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
    private String readCommand(String question, String... validCommands) {
        return readCommand(question, Arrays.asList(validCommands));
    }

    /**
     * Requests that the user enter a valid command from a list of possible commands
     *
     * @param question Prompt to provide to the user when listing valid commands
     * @param validCommandsList Valid commands
     * @return The command that the user entered
     */
    private String readCommand(String question, List<String> validCommandsList) {
        // Maintain a separate list for commands that are printed, excluding hidden commands
        List<String> printedCommandsList = new ArrayList<>();

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

        String command;
        while (true) {
            command = mInputScanner.nextLine().trim().toLowerCase();

            // Wait for text
            if (command.length() == 0) {
                continue;
            }

            // Exit the loop if the command is valid
            if (validCommandsList.contains(command)) {
                break;
            }

            System.err.println("Invalid command '" + command + "'");
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
            if (CHEAT_ENABLED) commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_CHEAT);
            commands.add(HIDDEN_COMMAND_PREFIX + COMMAND_QUIT);

            String command = readCommand("Choose action", commands);

            if (command.equalsIgnoreCase(COMMAND_QUIT)) {
                System.out.println("The solution was: " + mGame.getSolution().asHumanReadableString());
                System.exit(0);
            } else if (command.equalsIgnoreCase(COMMAND_CHEAT)) {
                System.out.println("The solution is: " + mGame.getSolution().asHumanReadableString());
            } else if (command.equalsIgnoreCase(COMMAND_DONE)) {
                break;
            } else if (command.equalsIgnoreCase(COMMAND_ROLL)) {
                int[] dice = new int[2];
                int remainingMovements = mGame.rollDice(dice);
                if (remainingMovements < 0) {
                    continue;
                }

                System.out.println("Rolled " + dice[0] + " + " + dice[1] + " = " + remainingMovements);

                Location startLocation = mGame.getCurrentPlayerLocation();
                if (startLocation.isRoom()) {
                    List<RoomTile> corridorTiles = startLocation.asRoom().getEntranceCorridors();
                    String[] validCommands = new String[corridorTiles.size()];
                    for (int i = 1; i <= corridorTiles.size(); i++) {
                        validCommands[i - 1] = Integer.toString(i);
                    }
                    int entranceCorridor = Integer.parseInt(readCommand("Choose room exit", validCommands)) - 1;

                    remainingMovements = mGame.moveTo(corridorTiles.get(entranceCorridor).getDoorTile());
                }

                Tile moveTile = null;
                CorridorTile currentTile = mGame.getCurrentPlayerLocation().asTile();
                boolean moved = false;
                while (remainingMovements > 0) {
                    List<String> validCommands = new ArrayList<>(4);
                    if (currentTile == null) {
                        throw new IllegalStateException("Current tile cannot be null");
                    }
                    if (currentTile.canMoveLeft()) validCommands.add(COMMAND_LEFT);
                    if (currentTile.canMoveUp()) validCommands.add(COMMAND_UP);
                    if (currentTile.canMoveRight()) validCommands.add(COMMAND_RIGHT);
                    if (currentTile.canMoveDown()) validCommands.add(COMMAND_DOWN);
                    if (moved) validCommands.add(HIDDEN_COMMAND_PREFIX + COMMAND_STOP);

                    String direction = readCommand("Choose direction", validCommands);
                    if (direction.equalsIgnoreCase(COMMAND_STOP)) {
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
                            System.err.println("Can't return to same room");
                            continue;
                        }

                        mGame.moveTo(((RoomTile) moveTile).getRoom());
                        break;
                    } else {
                        currentTile = (CorridorTile) moveTile;
                    }

                    remainingMovements = mGame.moveTo(currentTile);
                    moved = true;
                }
            } else if (command.equalsIgnoreCase(COMMAND_PASSAGE)) {
                mGame.usePassage();
            } else if (command.equalsIgnoreCase(COMMAND_QUESTION)) {
                Suggestion suggestion = createSuggestion(mGame.getCurrentPlayerLocation().asRoom());
                Player matchingPlayer = mGame.poseQuestion(suggestion);

                if (matchingPlayer != null) {
                    List<Card> matchingCards = matchingPlayer.matchingSuggestionCards(suggestion);

                    passToPlayer(matchingPlayer);

                    Card shownCard = chooseCard("Choose which card to show", matchingCards);

                    passToPlayer(player);

                    System.out.println(matchingPlayer.getName() + " has " + shownCard.getName());
                    player.addKnowledge(shownCard);
                } else {
                    System.out.println("No players have any of the suggested cards");
                }

            } else if (command.equalsIgnoreCase(COMMAND_ACCUSE)) {
                Suggestion suggestion;
                boolean correct;
                do {
                    suggestion = createSuggestion(null);
                    correct = mGame.makeFinalAccusation(suggestion);
                } while (readCommand("Are you sure? Final accusation: " + suggestion.asHumanReadableString(), COMMAND_YES, COMMAND_NO).equalsIgnoreCase(COMMAND_NO));

                if (correct) {
                    System.out.println("Congratulations! You were correct!");
                } else {
                    System.out.println("Your guess was incorrect. You have been eliminated.");
                }
            } else if (command.equalsIgnoreCase(COMMAND_NOTES)) {
                // TODO
            } else if (command.equalsIgnoreCase(COMMAND_LOG)) {
                // TODO
            }
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

        String card = readCommand(question, names);
        return map.get(card);
    }

    /**
     * Opens a dialog and allows for players to be registered to the game.
     */
    private void setupPlayers() {
        // TODO: Create selection dialog
        List<Suspect> suspects = mGame.getBoard().getSuspects();

        // TODO: Temporarily insert all suspects as players
        //for (Suspect suspect : suspects) {
        //    mGame.addPlayer(new Player(suspect, suspect.getName()));
        //}

        for (int i = 0; i < 3; i++) {
            mGame.addPlayer(new Player(suspects.get(i), suspects.get(i).getName()));
        }
    }

    /**
     * Requests that the game be passed to a player.
     *
     * @param p Player to pass to.
     */
    private void passToPlayer(Player p) {
        // TODO: Dialog requesting player
        System.out.println("Please pass to " + p.getName() + " and press enter to continue");
        mInputScanner.nextLine();
    }

    /**
     * Creates a new {@code Suggestion} from user selection in a dialog.
     *
     * If {@code currentRoom} is provided then that room is used, otherwise room is prompted too.
     *
     * @param currentRoom Player's current room, used in the suggestion if not null.
     * @return Suggestion created by user.
     */
    private Suggestion createSuggestion(Room currentRoom) {
        // TODO: Create selection dialog
        List<Suspect> suspects = mGame.getBoard().getSuspects();
        List<Weapon> weapons = mGame.getBoard().getWeapons();
        List<Room> rooms = mGame.getBoard().getRooms();

        Suspect suspect = chooseCard("Choose suspect", suspects);
        Weapon weapon = chooseCard("Choose weapon", weapons);
        Room room = currentRoom == null ? chooseCard("Choose room", rooms) : currentRoom;

        return new Suggestion(room, suspect, weapon);
    }

    /**
     * Initializes main UI
     */
    private void setupFrame() {
        mMainFrame = new JFrame();
        mMainFrame.setTitle(Config.TITLE);
        mMainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mMainFrame.setResizable(false);

        Rectangle windowSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        mTileSize = (int) (windowSize.getHeight() * 0.80 / Config.Board.HEIGHT);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mMainFrame.setContentPane(mainPanel);

        setupBoard();

        JPanel terminal = new JPanel();
        terminal.setLayout(new BoxLayout(terminal, BoxLayout.Y_AXIS));

        mOutputPanel = new OutputPanel();
        mInputPanel = new InputPanel();

        JScrollPane outputScrollPane = new JScrollPane(mOutputPanel);
        outputScrollPane.setPreferredSize(mOutputPanel.getPreferredSize());
        outputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        outputScrollPane.setPreferredSize(new Dimension((int)(screenSize.width * 0.25), (int)(screenSize.height * 0.4)));

        terminal.add(outputScrollPane);
        terminal.add(mInputPanel);

        System.setOut(new PrintStream(mOutputPanel.getOutputStream()));
        System.setErr(System.out);
        System.setIn(mInputPanel.getInputStream());

        mMainFrame.add(terminal);

        mMainFrame.pack();
        mMainFrame.setVisible(true);
    }

    /**
     * Initializes board UI
     */
    private void setupBoard() {
        // Setup layered pane container
        mBoardLayeredPane = new JLayeredPane();
        mBoardLayeredPane.setBackground(TileComponent.COLOR_EMPTY);
        mBoardLayeredPane.setOpaque(true);
        mMainFrame.add(mBoardLayeredPane);
        mBoardLayeredPane.setPreferredSize(new Dimension(mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT));

        // Add tile panel
        mBoardTilePanel = new JPanel();
        mBoardTilePanel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));
        mBoardTilePanel.setBounds(0, 0, mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT);
        mBoardTilePanel.setOpaque(false);

        mBoardLayeredPane.add(mBoardTilePanel, BOARD_LAYER_TILES);

        // Add room labels
        for (Room r : mGame.getBoard().getRooms()) {
            JLabel label = new JLabel(r.getName().toUpperCase());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) (mTileSize * 0.65f)));

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

        // TODO: Testing pathfinder code
        Runnable update = () -> {
            if (mToken == null || mEndLocation == null) {
                if (mTimer != null) mTimer.stop();
                return;
            }

            List<TokenOccupiableTile> path = PathFinder.findShortestPathAdvanced(mToken.getLocation(), mEndLocation, 101);
            if (path == null) {
                path = Collections.singletonList(mToken.getTile());
            }

            final Iterator<TokenOccupiableTile> finalPath = path.iterator();
            if (mTimer != null) mTimer.stop();
            mTimer = new Timer(100, evt -> {
                TokenOccupiableTile next = finalPath.next();
                mToken.setLocation(next instanceof RoomTile ? ((RoomTile) next).getRoom() : (CorridorTile) next);
                if (!finalPath.hasNext()) {
                    mTimer.stop();
                }
            });
            mTimer.setRepeats(true);
            mTimer.start();

            for (int y = 0; y < Config.Board.HEIGHT; y++) {
                for (int x = 0; x < Config.Board.WIDTH; x++) {
                    Tile tile = mGame.getBoard().getTile(x, y);
                    TileComponent button = (TileComponent) mBoardTilePanel.getComponent(BoardModel.tileCoordinatesToOffset(x, y));

                    if (tile instanceof CorridorTile) {
                        if (path.contains(tile)) {
                            button.setBackgroundColors(Color.GREEN, Color.GREEN.darker());
                        } else {
                            if ((y + x) % 2 == 0) {
                                button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_A, TileComponent.COLOR_CORRIDOR_A.brighter());
                            } else {
                                button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_B, TileComponent.COLOR_CORRIDOR_B.brighter());
                            }
                        }
                    }
                }
            }
        };

        // Add tiles
        for (int y = 0; y < Config.Board.HEIGHT; y++) {
            for (int x = 0; x < Config.Board.WIDTH; x++) {
                Tile tile = mGame.getBoard().getTile(x, y);
                TileComponent button = new TileComponent(tile);
                tile.setButton(button);
                button.setPreferredSize(new Dimension(mTileSize, mTileSize));

                // Tile borders
                char[] bc = BoardModel.getTileBordersAndCorners(x, y);
                if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
                    button.setBorder(new TileBorder(bc));
                }

                // TODO: Testing pathfinder code
                if (tile instanceof TokenOccupiableTile) {
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            mToken = ((TokenOccupiableTile) tile).getToken();

                            update.run();
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            mEndLocation = tile instanceof RoomTile ? ((RoomTile) tile).getRoom() : (CorridorTile) tile;
                            update.run();
                        }
                    });
                }

                // Add start tile background circle
                if (tile instanceof StartTile) {
                    mBoardLayeredPane.add(new StartTileCircle((StartTile) tile, mTileSize), BOARD_LAYER_START_TILE_CIRCLES);
                }

                // Set colors
                if (tile instanceof PassageTile) {
                    button.setBackgroundColors(TileComponent.COLOR_PASSAGE, TileComponent.COLOR_PASSAGE.brighter());
                } else if (tile instanceof RoomTile) {
                    button.setBackgroundColors(TileComponent.COLOR_ROOM, TileComponent.COLOR_ROOM.brighter());
                } else if (tile instanceof CorridorTile) {
                    if ((y + x) % 2 == 0) {
                        button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_A, TileComponent.COLOR_CORRIDOR_A.brighter());
                    } else {
                        button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_B, TileComponent.COLOR_CORRIDOR_B.brighter());
                    }
                } else if (tile instanceof EmptyTile) {
                    button.setEnabled(false);
                    button.setOpaque(false);
                }

                mBoardTilePanel.add(button);
            }
        }
    }
}
