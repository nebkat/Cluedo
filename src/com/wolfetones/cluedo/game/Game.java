package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.card.Room;
import com.wolfetones.cluedo.card.Suspect;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Game {
	private Set<Card> mCards;

	private Suggestion mAnswer;

	private List<Player> mPlayers;
	
	private static final int TILE_SIZE = 70;

	private static Tile mStartTile = null;
	private static Tile mEndTile = null;

	private Timer mTimer;

	private BoardModel mBoard = new BoardModel();

	public static void main(String[] args) {
		System.out.println("Welcome to " + Config.TITLE + " by");
		System.out.println(Config.AUTHOR);

		new Game();
	}

    public Game() {
	    JFrame frame = new JFrame();
	    frame.setTitle(Config.TITLE);
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	    JLayeredPane layeredPane = new JLayeredPane();
	    layeredPane.setBackground(TileComponent.COLOR_EMPTY);
	    layeredPane.setOpaque(true);
	    frame.setContentPane(layeredPane);
	    layeredPane.setPreferredSize(new Dimension(TILE_SIZE * Config.Board.WIDTH, TILE_SIZE * Config.Board.HEIGHT));
	    frame.pack();

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));
		panel.setBounds(0, 0, TILE_SIZE * Config.Board.WIDTH, TILE_SIZE * Config.Board.HEIGHT);
		panel.setOpaque(false);

		layeredPane.add(panel, new Integer(2));

		for (Room r : mBoard.getRooms()) {
			JLabel label = new JLabel(r.getName().toUpperCase());
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setFont(new Font("sans-serif", Font.BOLD, 45));

			int centerX = (int) (r.getCenterX() * TILE_SIZE);
			int centerY = (int) (r.getCenterY() * TILE_SIZE);

			label.setBounds(centerX - 250, centerY - 100, 500, 200);

			layeredPane.add(label, new Integer(3));
		}

		Token token = new SuspectToken(TILE_SIZE, new Suspect(10, "Hello", Color.BLACK));
		token.setPosition(8, 7);

		layeredPane.add(token, new Integer(5));


		Runnable update = () -> {
			if (mStartTile == null || mEndTile == null) {
				return;
			}

			mStartTile = token.getTile();

			List<Tile> path = null;
			if (mEndTile instanceof RoomTile) {
				for (Tile t : ((RoomTile) mEndTile).getRoom().getEntranceCorridors()) {
					List<Tile> p = PathFinder.findShortestPath(mStartTile, t, 101);
					if (p == null) continue;
					if (path == null || p.size() < path.size()) {
						path = p;
					}
				}
			} else {
				path = PathFinder.findShortestPath(mStartTile, mEndTile, 120);
			}

			if (path == null) {
				path = Collections.singletonList(mStartTile);
			}

			final Iterator<Tile> finalPath = path.iterator();
			if (mTimer != null) mTimer.stop();
			mTimer = new Timer(100, evt -> {
				Tile next = finalPath.next();
				token.setTile(next);
				if (!finalPath.hasNext()) {
					mTimer.stop();
				}
			});
			mTimer.setRepeats(true);
			mTimer.start();

			for (int y = 0; y < Config.Board.HEIGHT; y++) {
				for (int x = 0; x < Config.Board.WIDTH; x++) {
					Tile tile = mBoard.getTile(x, y);
					TileComponent button = (TileComponent) panel.getComponent(BoardModel.tileCoordinatesToOffset(x, y));

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
	    		Tile tile = mBoard.getTile(x, y);
	    		TileComponent button = new TileComponent(tile);
	    		tile.setButton(button);
	    		button.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));

	    		char[] bc = BoardModel.getTileBordersAndCorners(x, y);
	    		if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
					button.setBorder(new TileBorder(bc));
				}

	    		button.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						mStartTile = tile;
						token.setTile(tile);
						update.run();
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						mEndTile = tile;
						update.run();
					}
				});

	    		if (tile instanceof StartTile) {
	    			layeredPane.add(new StartTileCircle((StartTile) tile, TILE_SIZE), new Integer(1));

					Token t = new SuspectToken(TILE_SIZE, ((StartTile) tile).getStartingSuspect());
					t.setTile(tile);

					layeredPane.add(t, new Integer(5));
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

				panel.add(button);
			}
		}

	    frame.setVisible(true);
    }
}
