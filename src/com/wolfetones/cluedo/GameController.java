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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {
    private static final boolean CHEAT_ENABLED = true;

    private static final String HIDDEN_COMMAND_PREFIX = "@";

    private static final String COMMAND_ROLL = "roll";
    private static final String COMMAND_PASSAGE = "passage";
    private static final String COMMAND_DONE = "done";
    private static final String COMMAND_QUIT = "quit";
    private static final String COMMAND_QUESTION = "question";
    private static final String COMMAND_ACCUSE = "accuse";
    private static final String COMMAND_NOTES = "notes";
    private static final String COMMAND_CHEAT = "cheat";

    private static final String COMMAND_LEFT = "l";
    private static final String COMMAND_UP = "u";
    private static final String COMMAND_RIGHT = "r";
    private static final String COMMAND_DOWN = "d";
    private static final String COMMAND_STOP = "stop";

    private static final String COMMAND_YES = "y";
    private static final String COMMAND_NO = "n";


    private Game mGame = new Game();

    private int mTileSize;

    private JFrame mMainFrame;
    private JLayeredPane mBoardLayeredPane;
    private JPanel mBoardTilePanel;

    private static Tile mStartTile = null;
    private static Tile mEndTile = null;

    private static Token mToken = null;

    private Timer mTimer;

    private Scanner mInputScanner;

    public static void main(String[] args) {
        System.out.println("Welcome to " + Config.TITLE + " by");
        System.out.println(Config.AUTHOR);

        new GameController();
    }

    private GameController() {
        mInputScanner = new Scanner(System.in);

        selectPlayers();
        setupFrame();


        mGame.start();

        while (!mGame.isFinished()) {
            performTurn();
        }
    }

    /**
     * Requests that the user enter a valid command from a list of possible commands
     *
     * Allows input as array instead of list
     */
    private String readCommand(String question, String... validCommands) {
        return readCommand(question, Arrays.asList(validCommands));
    }

    /**
     * Requests that the user enter a valid command from a list of possible commands
     *
     * @param question Question to ask the user when listing valid commands
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

        String command;
        while (true) {
            command = mInputScanner.nextLine();

            // Exit the loop if the command is valid
            if (validCommandsList.contains(command)) {
                break;
            }

            System.err.println("Invalid command '" + command + "'");
        }

        return command;
    }

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
                System.out.println("The solution was: " + mGame.getSolution().asSuggestionString());
                System.exit(0);
            } else if (command.equalsIgnoreCase(COMMAND_CHEAT)) {
                System.out.println("The solution is: " + mGame.getSolution().asSuggestionString());
            } else if (command.equalsIgnoreCase(COMMAND_DONE)) {
                break;
            } else if (command.equalsIgnoreCase(COMMAND_ROLL)) {
                int[] dice = new int[2];
                mGame.rollDice(dice);
                int remainingMovements = dice[0] + dice[1];
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
                Suggestion suggestion = makeSuggestion(mGame.getCurrentPlayerLocation().asRoom());
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
                    suggestion = makeSuggestion(null);
                    correct = mGame.makeFinalAccusation(suggestion);
                } while (readCommand("Are you sure? Final accusation: " + suggestion.asSuggestionString(), COMMAND_YES, COMMAND_NO).equalsIgnoreCase(COMMAND_NO));

                if (correct) {
                    System.out.println("Congratulations! You were correct!");
                } else {
                    System.out.println("Your guess was incorrect. You have been eliminated.");
                }
            } else if (command.equalsIgnoreCase(COMMAND_NOTES)) {

            }
        }
    }

    private <T extends Card> T chooseCard(String question, List<T> cards) {
        List<String> names = cards.stream().map(Card::getShortName).collect(Collectors.toList());
        Map<String, T> map = cards.stream().collect(Collectors.toMap(Card::getShortName, Function.identity()));

        String card = readCommand(question, names);
        return map.get(card);
    }

    private void selectPlayers() {
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

    private void passToPlayer(Player p) {
        // TODO: Dialog requesting player
        System.out.println("Please pass to " + p.getName());
    }

    private Suggestion makeSuggestion(Room currentRoom) {
        // TODO: Create selection dialog
        List<Suspect> suspects = mGame.getBoard().getSuspects();
        List<Weapon> weapons = mGame.getBoard().getWeapons();
        List<Room> rooms = mGame.getBoard().getRooms();

        Suspect suspect = chooseCard("Choose suspect", suspects);
        Weapon weapon = chooseCard("Choose weapon", weapons);
        Room room = currentRoom == null ? chooseCard("Choose room", rooms) : currentRoom;

        return new Suggestion(room, suspect, weapon);
    }

    private void setupFrame() {
        mMainFrame = new JFrame();
        mMainFrame.setTitle(Config.TITLE);
        mMainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mMainFrame.setResizable(false);

        Rectangle windowSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        mTileSize = (int) (windowSize.getHeight() * 0.80 / Config.Board.HEIGHT);

        setupBoard();

        mMainFrame.setVisible(true);
    }

    private void setupBoard() {
        mBoardLayeredPane = new JLayeredPane();
        mBoardLayeredPane.setBackground(TileComponent.COLOR_EMPTY);
        mBoardLayeredPane.setOpaque(true);
        mMainFrame.setContentPane(mBoardLayeredPane);
        mBoardLayeredPane.setPreferredSize(new Dimension(mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT));
        mMainFrame.pack();

        mBoardTilePanel = new JPanel();
        mBoardTilePanel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));
        mBoardTilePanel.setBounds(0, 0, mTileSize * Config.Board.WIDTH, mTileSize * Config.Board.HEIGHT);
        mBoardTilePanel.setOpaque(false);

        mBoardLayeredPane.add(mBoardTilePanel, new Integer(2));

        for (Room r : mGame.getBoard().getRooms()) {
            JLabel label = new JLabel(r.getName().toUpperCase());
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setFont(new Font("sans-serif", Font.BOLD, (int) (mTileSize * 0.65f)));

            int centerX = (int) (r.getCenterX() * mTileSize);
            int centerY = (int) (r.getCenterY() * mTileSize);

            label.setBounds(centerX - 250, centerY - 100, 500, 200);

            mBoardLayeredPane.add(label, new Integer(3));
        }

        Runnable update = () -> {
            if (mToken == null || mEndTile == null) {
                if (mTimer != null) mTimer.stop();
                return;
            }

            List<Tile> path = null;
            if (mEndTile instanceof RoomTile) {
                for (RoomTile t : ((RoomTile) mEndTile).getRoom().getEntranceCorridors()) {
                    List<Tile> p = PathFinder.findShortestPath(mToken.getTile(), t, 101);
                    if (p == null) continue;
                    if (path == null || p.size() < path.size()) {
                        path = p;
                    }
                }
            } else {
                path = PathFinder.findShortestPath(mToken.getTile(), mEndTile, 120);
            }

            if (path == null) {
                path = Collections.singletonList(mToken.getTile());
            }

            final Iterator<Tile> finalPath = path.iterator();
            if (mTimer != null) mTimer.stop();
            mTimer = new Timer(100, evt -> {
                Tile next = finalPath.next();
                mToken.setTile(next);
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


        for (int y = 0; y < Config.Board.HEIGHT; y++) {
            for (int x = 0; x < Config.Board.WIDTH; x++) {
                Tile tile = mGame.getBoard().getTile(x, y);
                TileComponent button = new TileComponent(tile);
                tile.setButton(button);
                button.setPreferredSize(new Dimension(mTileSize, mTileSize));

                char[] bc = BoardModel.getTileBordersAndCorners(x, y);
                if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
                    button.setBorder(new TileBorder(bc));
                }

                if (tile instanceof TokenOccupiableTile) {
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            mStartTile = tile;
                            mToken = ((TokenOccupiableTile) tile).getToken();

                            update.run();
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            mEndTile = tile;
                            update.run();
                        }
                    });
                }

                if (tile instanceof StartTile) {
                    mBoardLayeredPane.add(new StartTileCircle((StartTile) tile, mTileSize), new Integer(1));

                    TokenComponent t = new SuspectTokenComponent(((StartTile) tile).getStartingSuspect(), mTileSize);

                    mBoardLayeredPane.add(t, new Integer(5));
                }

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
