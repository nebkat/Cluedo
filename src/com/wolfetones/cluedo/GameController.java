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
import com.wolfetones.cluedo.ui.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class GameController {
    private static final String COMMAND_ROLL = "roll";
    private static final String COMMAND_PASSAGE = "passage";
    private static final String COMMAND_DONE = "done";
    private static final String COMMAND_QUIT = "quit";

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

        performMove();
    }

    private void performMove() {
        Player player = mGame.getCurrentPlayer();

        System.out.println(player.getName() + "'s move (" + player.getCharacter().getName() + ")");
        System.out.println("Please enter a command:");

        String command = mInputScanner.nextLine();
        if (command.equals(COMMAND_ROLL)) {
            int movements = mGame.rollDice();

            if (movements < 0) {
                performMove();
                return;
            }

            System.out.println("Rolled " + movements);

            
        } else if (command.equals(COMMAND_PASSAGE)) {
            mGame.usePassage();
        }

        Tile currentTile = mGame.getCurrentPlayerTile();
        if (currentTile instanceof RoomTile) {
            System.out.println("Guessing");
        }
    }

    private void selectPlayers() {
        // TODO: Create selection dialog
        List<Suspect> suspects = mGame.getBoard().getSuspects();

        // TODO: Temporarily insert all suspects as players
        for (Suspect suspect : suspects) {
            mGame.addPlayer(new Player(suspect, suspect.getName()));
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
