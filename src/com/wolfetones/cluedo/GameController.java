package com.wolfetones.cluedo;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.card.Token;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.game.Game;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {
    private static final String COMMAND_ROLL = "roll";
    private static final String COMMAND_PASSAGE = "passage";
    private static final String COMMAND_DONE = "done";
    private static final String COMMAND_QUIT = "quit";
    private static final String COMMAND_CHEAT = "cheat";

    private static final String COMMAND_LEFT = "l";
    private static final String COMMAND_UP = "u";
    private static final String COMMAND_RIGHT = "r";
    private static final String COMMAND_DOWN = "d";


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

        while (true) {
            performMove();
        }
    }

    private String readCommand(String question, String... validCommands) {
        List<String> validCommandsList = Arrays.asList(validCommands);
        System.out.println(question + " [valid: " + Util.implode(validCommands, ", ") + "]");
        String command;
        while (true) {
            command = mInputScanner.nextLine();

            if (validCommandsList.contains(command)) {
                break;
            }

            System.err.println("Invalid command '" + command + "'");
        }

        return command;
    }

    private void performMove() {
        Player player = mGame.nextMove();

        System.out.println(player.getName() + "'s move (" + player.getCharacter().getName() + ")");

        boolean success = false;
        do {
            String command = readCommand("Enter command", COMMAND_ROLL, COMMAND_PASSAGE, COMMAND_QUIT, COMMAND_CHEAT);
            if (command.equals(COMMAND_ROLL)) {
                int remainingMovements = mGame.rollDice();

                if (remainingMovements < 0) {
                    continue;
                }

                System.out.println("Rolled " + remainingMovements);
                success = true;

                Tile moveTile = mGame.getCurrentPlayerTile();

                Room startingRoom = null;
                if (moveTile instanceof RoomTile) {
                    startingRoom = ((RoomTile) moveTile).getRoom();
                    List<CorridorTile> corridorTiles = ((RoomTile) moveTile).getRoom().getEntranceCorridors();
                    String[] validCommands = new String[corridorTiles.size()];
                    for (int i = 1; i <= corridorTiles.size(); i++) {
                        validCommands[i - 1] = Integer.toString(i);
                    }
                    String entranceCorridor = readCommand("Choose room exit", validCommands);

                    moveTile = corridorTiles.get(Integer.parseInt(entranceCorridor) - 1);
                    player.getCharacter().setTile(moveTile);

                    remainingMovements--;
                }

                CorridorTile currentTile = (CorridorTile) moveTile;
                while (remainingMovements > 0) {
                    List<String> validCommands = new ArrayList<>(4);
                    if (currentTile.canMoveLeft()) validCommands.add(COMMAND_LEFT);
                    if (currentTile.canMoveUp()) validCommands.add(COMMAND_UP);
                    if (currentTile.canMoveRight()) validCommands.add(COMMAND_RIGHT);
                    if (currentTile.canMoveDown()) validCommands.add(COMMAND_DOWN);

                    String direction = readCommand("Choose direction", validCommands.toArray(new String[validCommands.size()]));
                    switch (direction) {
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
                        if (((RoomTile) moveTile).getRoom() == startingRoom) {
                            System.err.println("Can't return to same room");
                            continue;
                        }

                        break;
                    } else {
                        currentTile = (CorridorTile) moveTile;
                    }

                    remainingMovements--;

                    player.getCharacter().setTile(currentTile);
                }

                if (moveTile instanceof RoomTile) {
                    moveTile = ((RoomTile) moveTile).getRoom().getNextUnoccupiedTile();
                }

                player.getCharacter().setTile(moveTile);
            } else if (command.equals(COMMAND_PASSAGE)) {
                success = mGame.usePassage();
            } else if (command.equals(COMMAND_QUIT)) {
                System.out.println("The solution was: " + mGame.getSolution().asSuggestionString());
                System.exit(0);
            } else if (command.equals(COMMAND_CHEAT)) {
                System.out.println("The solution is: " + mGame.getSolution().asSuggestionString());
            }
        } while (!success);

        Tile currentTile = mGame.getCurrentPlayerTile();
        if (currentTile instanceof RoomTile) {
            System.out.println("Guessing");
        }
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

            java.util.List<Tile> path = null;
            if (mEndTile instanceof RoomTile) {
                for (Tile t : ((RoomTile) mEndTile).getRoom().getEntranceCorridors()) {
                    java.util.List<Tile> p = PathFinder.findShortestPath(mToken.getTile(), t, 101);
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
                    ((StartTile) tile).getStartingSuspect().setTile(tile);

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
