package com.wolfetones.cluedo.game;

import com.wolfetones.cluedo.board.BoardModel;
import com.wolfetones.cluedo.board.PathFinder;
import com.wolfetones.cluedo.board.tiles.*;
import com.wolfetones.cluedo.card.Card;
import com.wolfetones.cluedo.config.Config;
import com.wolfetones.cluedo.ui.TileBorder;
import com.wolfetones.cluedo.ui.TileComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Game {
	private Set<Card> mCards;

	private Suggestion mAnswer;

	private List<Player> mPlayers;

	private static Tile mStartTile = null;
	private static Tile mEndTile = null;

	private BoardModel mBoard = new BoardModel();

	public static void main(String[] args) {
		System.out.println("Welcome to " + Config.TITLE + " by " + Config.AUTHOR);

		new Game();
	}

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	    JFrame frame = new JFrame();
	    frame.setTitle(Config.TITLE);
	    frame.setSize((int) (screenSize.getWidth() * 0.9), (int) (screenSize.getHeight() * 0.9));
	    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(Config.Board.HEIGHT, Config.Board.WIDTH));

		Runnable update = () -> {
			if (mStartTile == null || mEndTile == null) {
				return;
			}

			List<Tile> path = null;
			if (mEndTile instanceof RoomTile) {
				for (Tile t : ((RoomTile) mEndTile).getRoom().getEntranceCorridors()) {
					List<Tile> p = PathFinder.findShortestPath(mStartTile, t, 11);
					if (p == null) continue;
					if (path == null || p.size() < path.size()) {
						path = p;
					}
				}
			} else {
				path = PathFinder.findShortestPath(mStartTile, mEndTile, 12);
			}

			if (path == null) {
				path = Collections.singletonList(mStartTile);
			}

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
	    		button.setPreferredSize(new Dimension(70, 70));

	    		char[] bc = BoardModel.getTileBordersAndCorners(x, y);
	    		if (!IntStream.range(0, bc.length).mapToObj((i) -> bc[i]).allMatch((c) -> c == Config.Board.Tiles.EMPTY)) {
					button.setBorder(new TileBorder(bc));
				}

	    		button.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						mStartTile = tile;
						update.run();
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						mEndTile = tile;
						update.run();
					}
				});

				if (tile instanceof RoomTile) {
					button.setBackgroundColors(TileComponent.COLOR_ROOM, TileComponent.COLOR_ROOM.brighter());
				} else if (tile instanceof CorridorTile) {
					if ((y + x) % 2 == 0) {
						button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_A, TileComponent.COLOR_CORRIDOR_A.brighter());
					} else {
						button.setBackgroundColors(TileComponent.COLOR_CORRIDOR_B, TileComponent.COLOR_CORRIDOR_B.brighter());
					}
				} else if (tile instanceof EmptyTile) {
	    			button.setEnabled(false);
					button.setBackground(TileComponent.COLOR_EMPTY);
				}

				panel.add(button);
			}
		}

	    frame.add(panel);

	    frame.pack();
	    frame.setVisible(true);
    }
}
